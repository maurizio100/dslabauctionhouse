package auction.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import auction.global.communication.LocalMessageController;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;

public class ClientUDPPort extends Thread implements IExitObserver{

	private ILocalMessageReceiver localMessenger = null;
	private int udpPort = -1;
	private DatagramSocket socket = null;

	public ClientUDPPort(LocalMessageController lmc, int udpPort, IExitSender e) throws SocketException{
		this.udpPort = udpPort;
		localMessenger = lmc;
		e.registerExitObserver(this);
		initSocket();
		this.start();
	}

	private void initSocket() throws SocketException{
		socket = new DatagramSocket( udpPort );		
	}

	public void run(){
		byte[] data = new byte[1024];
		String message = null;
		try{
			while( !this.isInterrupted() ){
				
				DatagramPacket packet = new DatagramPacket(data,  data.length);
				socket.receive(packet);
				data = packet.getData();
				message = new String(data, 0, packet.getLength());
				
				String[] splitted = message.split("\n");
				for( int i=0; i < splitted.length; i++ ){
					sendMessageToLocalMessenger(splitted[i]);
				}
			}
			
		}catch( IOException e){sendMessageToLocalMessenger("UDPPort shut down - socket was closed.");}

	}
	
	private void sendMessageToLocalMessenger(String message){
		localMessenger.receiveLocalMessage(message);
	}

	@Override
	public void exit() {
		socket.close();
		this.interrupt();
		
	}

}
