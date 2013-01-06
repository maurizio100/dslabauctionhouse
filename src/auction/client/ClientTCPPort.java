package auction.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import auction.exceptions.ServerDisconnectedException;
import auction.interfaces.IExitObserver;
import auction.interfaces.IExitSender;
import auction.interfaces.IMessageReceiver;
import auction.interfaces.IMessageSender;

public class ClientTCPPort extends Thread implements IMessageReceiver, IExitObserver{

	private IMessageSender nwMessageSender = null;
	private IMessageReceiver localMessenger = null;
	private Socket serverConnection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private String host;
	private int port;

	public ClientTCPPort(String host, int port, IMessageSender snd, IMessageReceiver rcv, IExitSender e) 
			throws UnknownHostException, IOException{

		nwMessageSender = snd;
		nwMessageSender.registerMessageReceiver(this);
		e.registerExitObserver(this);

		localMessenger = rcv;
		this.host = host;
		this.port = port;

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
					sendMessageToLocalMessenger(inString);
				}
				if(inString == null) throw new ServerDisconnectedException();
			}
		}catch( IOException e ){
			sendMessageToLocalMessenger("Shutting down ClientTCP - Socket inputstream closed.");
		}catch( ServerDisconnectedException sde){
//			sendMessageToLocalMessenger("Push ENTER to completely shutdown the Program.");
//			localMessenger.invokeShutdown();
			localMessenger.switchToOfflineMode();
			sendMessageToLocalMessenger("The Server is not online anymore. Client is going to shutdown now.");
		}

	}

	private void sendMessageToServer(String message) {
		out.println(message);
	}

	private void sendMessageToLocalMessenger(String message){
		localMessenger.receiveMessage(message);
	}

	@Override
	public void receiveMessage(String message){
		sendMessageToServer(message);
	}

	@Override
	public void exit() {
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

	@Override
	public void invokeShutdown() {
		this.exit();
	}

	@Override
	public void switchToOfflineMode() {
		// TODO Auto-generated method stub
		
	}
}
