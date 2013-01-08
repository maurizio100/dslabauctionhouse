package auction.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import auction.client.interfaces.IClientMessageForwarder;
import auction.client.interfaces.INetworkSocket;
import auction.global.exceptions.ServerDisconnectedException;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;
import auction.global.interfaces.ILocalMessageSender;

public class ClientTCPPort extends Thread 
implements INetworkSocket{

	private Socket serverConnection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private IClientMessageForwarder nwcontroller = null;

	public ClientTCPPort(String host, int port, IClientMessageForwarder nwcontroller ) 
			throws UnknownHostException, IOException{

		this.nwcontroller = nwcontroller;

		serverConnection = new Socket(host,port);	
		out = new PrintWriter(serverConnection.getOutputStream(), true);
		in = new BufferedReader( new InputStreamReader(serverConnection.getInputStream()));

		this.start();
	}

	public void run(){

		try{
			String inString = null;

			while(true){

				if( (inString = in.readLine()) != null ){
					sendMessageToClient(inString);
				}
				if(inString == null) throw new ServerDisconnectedException();
			}
		}catch( IOException e ){
			sendMessageToClient("Shutting down ClientTCP - Socket inputstream closed.");
		}catch( ServerDisconnectedException sde){
			//			sendMessageToLocalMessenger("Push ENTER to completely shutdown the Program.");
			//			localMessenger.invokeShutdown();
			//			localMessenger.switchToOfflineMode();
			sendMessageToClient("The Server is not online anymore. Client is going to shutdown now.");
		}

	}

	private void sendMessageToServer(String message) {
		out.println(message);
	}

	private void sendMessageToClient(String message){
		nwcontroller.sendMessageToClient(message);
	}

	@Override
	public void sendMessageToNetwork(String message){
		sendMessageToServer(message);
	}

	@Override
	public void shutDownSocket() {
		try{		
			/*	sendMessageToLocalMessenger("Shutting down ClientTCP");*/
			serverConnection.close();

		}catch(IOException e){}
		finally{
			if(serverConnection != null ){
				try{in.close();}catch (IOException e){}
				out.close();
			}
		}
	}
}
