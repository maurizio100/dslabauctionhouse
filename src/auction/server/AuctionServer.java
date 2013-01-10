package auction.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import auction.global.communication.CommandController;
import auction.global.communication.LocalMessageController;
import auction.global.crypt.RSACrypt;
import auction.global.exceptions.PortRangeException;
import auction.global.io.IOUnit;

public class AuctionServer {

	public static final String WELCOME = "This is the Auction Server. Push Enter if you want to shutdown.";

	private static void usage(){
		System.err.println(
				"Usage -> java AuctionServer host tcpPort"
				);
	}

	public static void main(String[] args){

		int tcpPort = -1;
		String pathToPublicKey = null;
		String pathToDir = null;

		if( args.length != 5){ usage(); }

		try {
			pathToPublicKey = args[1];
			pathToDir = args[4];

			/* --- password request for server's private key --- */
			PEMReader in;
			String pathToPrivateKey = args[3];
			PrivateKey privateKey = null;
			try {
				in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
					@Override
					public char[] getPassword() {
						// reads the password from standard input for decrypting the private key
						System.out.println("Enter pass phrase:");
						try {
							return (new BufferedReader(new InputStreamReader(System.in)).readLine()).toCharArray();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null; 
					}
				});
				System.out.println(pathToPrivateKey);
				KeyPair keyPair = (KeyPair) in.readObject(); 
				privateKey = keyPair.getPrivate();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Passwortabfrage ende

			tcpPort = Integer.parseInt(args[0]);
			if( tcpPort < 1023 || tcpPort > 65536 ) 
				throw new PortRangeException("Port is out of Range! Valid ports are between 1024 and 65536.");

			LocalMessageController lmc = new LocalMessageController();
			CommandController cc = new CommandController();

			ClientManager clientManager = new ClientManager(cc, lmc);
			//ServerModel model = new ServerModel(lmc, cc, clientManager);
			ServerModel model = new ServerModel(tcpPort, lmc, cc, clientManager, pathToPublicKey, privateKey, pathToDir);
			model.registerExitObserver(clientManager);

			ServerTCPPort serverTCPPort = new ServerTCPPort(tcpPort, clientManager, lmc, model);
			model.setServerTCP(serverTCPPort);
			IOUnit ioUnit = new IOUnit(lmc, model, model, WELCOME);

		} catch (PortRangeException e) {
			e.getMessage();
		}catch( NumberFormatException e ){
			System.err.println("Port must be numeric!");
		}
	}
}
