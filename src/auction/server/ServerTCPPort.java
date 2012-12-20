package auction.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import auction.communication.ExitObserver;
import auction.communication.ExitSender;
import auction.communication.MessageReceiver;
import auction.exceptions.PortRangeException;

public class ServerTCPPort extends Thread implements ExitObserver{

	private BufferedReader in = null;
	private PrintWriter out = null;
	private Socket clientSocket = null;
	private int port = -1;
	private ServerSocket s = null;
	private ClientManager clientManager = null;
	private MessageReceiver localMessenger = null;
	
	public ServerTCPPort( int port, ClientManager cm, MessageReceiver rcv, ExitSender ex) throws PortRangeException{
		this.port = port;
		clientManager = cm;
		ex.registerExitObserver(this);
		localMessenger = rcv;
		this.start();
	}

	public void run(){
		try{
		
			s = new ServerSocket(port);
			while( !this.isInterrupted() ){
				clientSocket = s.accept();
				clientManager.addNewClient( clientSocket );
				clientSocket = null;
			}
		}catch(IOException e){
			localMessenger.receiveMessage("Shutting down ServerTCPPort due to an IOError");
		}
	}

	@Override
	public void exit() {
		localMessenger.receiveMessage("Shutting down ServerTCPPort.");
		try { s.close(); } catch (IOException e) {}
	}
}
