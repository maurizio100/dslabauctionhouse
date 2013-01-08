package auction.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import auction.global.interfaces.ICommandReceiver;
import auction.global.interfaces.ICrypt;
import auction.global.interfaces.ILocalMessageReceiver;
import auction.server.interfaces.IClientThread;

public class Client implements Runnable, IClientThread{

	private Socket activeSocket = null;
	private BufferedReader input = null;
	private PrintWriter output = null;

	private ICommandReceiver commandController = null;
	private ILocalMessageReceiver localMessenger = null;

	private String clientName = null;
	private InetAddress host = null;
	private int udpPort = -1;

	private boolean loggedIn = false;

	public Client(Socket socket, ICommandReceiver controller, ILocalMessageReceiver rcv) throws IOException{
		activeSocket = socket;
		commandController = controller;
		host = socket.getInetAddress();
		localMessenger = rcv;

		input = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
		output = new PrintWriter(activeSocket.getOutputStream(),true);
	}

	public void setLoginName(String client){
		clientName = client;
	}

	public void receiveFeedback(String feedback){
		output.println(feedback);
		output.flush();
	}

	@Override
	public void run() {
		String inputString;

		try{	
			while(!Thread.interrupted()){
				if( (inputString = input.readLine()) != null ){
					this.sendToCommandReceiver(inputString);
				}
			}
		}catch(IOException e){
			this.sendToLocalMessenger("There was an IOError by reading from Socket.");
		}
	}

	private void sendToCommandReceiver( String command ){
		commandController.receiveCommand(command, this);
	}

	private void sendToLocalMessenger( String message ){
		localMessenger.receiveLocalMessage(message);
	}

	@Override
	public void exit(){
		try{
			this.sendToLocalMessenger("Client at Host: " + host + " disconnected and logged out.");
			if(loggedIn){
				sendToCommandReceiver("!logout");		
			}
			activeSocket.close();
			Thread.currentThread().interrupt();
		}catch( IOException e){}
		finally{
			try{ input.close(); }catch(IOException e){}
			output.close();
		}
	}

	@Override
	public String getClientName() {
		return clientName;
	}

	@Override
	public int getUdpPort() {
		return udpPort;
	}

	@Override
	public InetAddress getHost() {
		return host;
	}

	@Override
	public void setLogin( String name, int preferredUDPPort) {
		this.loggedIn = true;
		clientName = name;
		udpPort = preferredUDPPort;

	}
	@Override
	public void setLogout(){
		this.loggedIn = false;
	}

	@Override
	public boolean isLoggedIn(){
		return loggedIn;
	}

}
