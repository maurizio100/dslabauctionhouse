package auction.server;

import auction.communication.CommandController;
import auction.communication.LocalMessageController;
import auction.exceptions.PortRangeException;
import auction.io.IOUnit;

public class AuctionServer {

	public static final String WELCOME = "This is the Auction Server. Push Enter if you want to shutdown.";

	private static void usage(){
		System.err.println(
				"Usage -> java AuctionServer host tcpPort"
				);
	}
	
	
	public static void main(String[] args){
		
		int tcpPort = -1;
		
		if( args.length != 1){ usage(); }
		
		try {
			tcpPort = Integer.parseInt(args[0]);
			if( tcpPort < 1023 || tcpPort > 65536 ) 
				throw new PortRangeException("Port is out of Range! Valid ports are between 1024 and 65536.");
			
			LocalMessageController lmc = new LocalMessageController();
			CommandController cc = new CommandController();

			ClientManager clientManager = new ClientManager(cc, lmc);
			ServerModel model = new ServerModel(lmc, cc, clientManager);
			model.registerExitObserver(clientManager);
			
			ServerTCPPort serverTCPPort = new ServerTCPPort(tcpPort, clientManager, lmc, model);
			IOUnit ioUnit = new IOUnit(lmc, model, model, WELCOME);
			
		} catch (PortRangeException e) {
			e.getMessage();
		}catch( NumberFormatException e ){
			System.err.println("Port must be numeric!");
		}
		
	}
}
