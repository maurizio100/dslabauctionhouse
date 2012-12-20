package auction.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerUDPPort {


	public void sendNotification(int udpPort, InetAddress host, String notification) throws IOException{
		InetAddress ia = host;
		int port = udpPort;
		byte[] data = notification.getBytes();
		
		DatagramPacket packet = new DatagramPacket(data, data.length, ia, port);
		DatagramSocket socket = new DatagramSocket();
		
		socket.send(packet);
	}
	
}
