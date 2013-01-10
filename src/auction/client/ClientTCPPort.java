package auction.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import auction.client.interfaces.IClientMessageForwarder;
import auction.client.interfaces.INetworkSocket;
import auction.global.exceptions.ServerDisconnectedException;

public class ClientTCPPort extends Thread 
implements INetworkSocket{

	private Socket serverConnection = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private IClientMessageForwarder nwcontroller = null;
	private boolean offlineSignal = false;
	private String servername = null;
	
	public ClientTCPPort(String host, int port, IClientMessageForwarder nwcontroller, boolean offlineSignal ) 
			throws UnknownHostException, IOException{

		this.nwcontroller = nwcontroller;

		serverConnection = new Socket(host,port);	
		out = new PrintWriter(serverConnection.getOutputStream(), true);
		in = new BufferedReader( new InputStreamReader(serverConnection.getInputStream()));

		this.offlineSignal = offlineSignal;
		this.start();
	}
	public ClientTCPPort(String host, int port, IClientMessageForwarder nwcontroller, boolean offlineSignal, String servername) 
			throws UnknownHostException, IOException{
		this(host, port, nwcontroller, offlineSignal);
		this.servername = servername;
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
//			sendInfoMessageToClient("Socket inputstream closed. Logging out Client!");
			sendServerDisconnectedSignal();
		}catch( ServerDisconnectedException sde){
//			sendInfoMessageToClient("The Server is not online anymore. Client is going to Log out!");
			sendServerDisconnectedSignal();
		}
		
	}


	private void sendServerDisconnectedSignal() {
		if( offlineSignal ) nwcontroller.sendDisconnectedSignal();
	}

	private void sendMessageToServer(String message) {
		out.println(message);
	}

	private void sendInfoMessageToClient(String message) {
		nwcontroller.sendNetworkStatusMessageToClient(message);
	}

	private void sendMessageToClient(String message){
		if(servername != null)
		{
			nwcontroller.sendMessageToClient(message, servername);
		}
		else
		{
			nwcontroller.sendMessageToClient(message);
		}
	}

	@Override
	public void sendMessageToNetwork(String message){
		sendMessageToServer(message);
	}
	

	@Override
	public void shutDownSocket() {
		try{		
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
	public Socket getConnection() {
		return serverConnection;
	}
}
