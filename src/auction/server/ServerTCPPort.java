package auction.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import auction.global.exceptions.PortRangeException;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;

public class ServerTCPPort extends Thread implements IExitObserver{

	private BufferedReader in = null;
	private PrintWriter out = null;
	private Socket clientSocket = null;
	private int port = -1;
	private ServerSocket s = null;
	private ClientManager clientManager = null;
	private ILocalMessageReceiver localMessenger = null;
	
	public ServerTCPPort( int port, ClientManager cm, ILocalMessageReceiver rcv, IExitSender ex) throws PortRangeException{
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
			localMessenger.receiveLocalMessage("Shutting down ServerTCPPort due to an IOError");
		}
	}

	@Override
	public void exit() {
		localMessenger.receiveLocalMessage("Shutting down ServerTCPPort.");
		try { s.close(); } catch (IOException e) {}
	}
}
