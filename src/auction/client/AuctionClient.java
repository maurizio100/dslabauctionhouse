package auction.client;

import java.io.IOException;
import java.net.UnknownHostException;

import auction.communication.LocalMessageController;
import auction.communication.NetworkMessageForwardingController;
import auction.exceptions.PortRangeException;
import auction.io.IOUnit;

public class AuctionClient {

	public static final String WELCOME = "Welcome to the DSlab Auction house! Please enter your command:";
	
	private static void usage(){
		System.err.println(
				"Usage -> java AuctionClient host tcpPort udpPort"
				);
	}
	
	public static void main(String[] args){
		
		String host = "";
		int tcpPort = -1;
		int udpPort = -1;
		ClientModel model = null;
		String pathToPublicKey = null;
		String pathToPrivateKey = null;
		/*--------------Arguments handling--------------------------------*/
		if( args.length != 5){ usage(); System.exit(0); }
	
		try{
			host = args[0];
			tcpPort = Integer.parseInt(args[1]);
			udpPort = Integer.parseInt(args[2]);
			pathToPublicKey = args[3];
			pathToPrivateKey = args[4];
			
		/*-------------Port check-----------------------------------------*/	
			if( tcpPort < 1023 || tcpPort > 65536 || udpPort < 1023 || udpPort > 65536) 
				throw new PortRangeException("Port is out of Range! Valid ports are between 1024 and 65536."); 

			/* Initialization of all classes */
			/*controllers for communication between all Client-objects*/
			LocalMessageController lmc = new LocalMessageController();
			NetworkMessageForwardingController nmfc = new NetworkMessageForwardingController();
			
			model = new ClientModel(lmc, nmfc, udpPort, pathToPublicKey, pathToPrivateKey);
			IOUnit iounit = new IOUnit( lmc, model, model, WELCOME );
			
			/*-----------Network components-----------------------------*/
			ClientTCPPort socketPort = new ClientTCPPort( host, tcpPort, nmfc, lmc, model );
			ClientUDPPort updPort = new ClientUDPPort( lmc, udpPort, model);
			
		}catch(NumberFormatException nfe){
			System.err.println("ClientMain: " +
					"tcpPort and udpPort must be numeric!");
		}catch(PortRangeException e){
			System.err.println(e.getMessage());
		} catch (UnknownHostException e) {
			System.err.println("Unkown Host! Program Shuts down.");
			model.sendExit();
		} catch (IOException e1){
			System.err.println("Server is not reachable! Program Shuts down.");
			model.sendExit();
		}
	}
}
