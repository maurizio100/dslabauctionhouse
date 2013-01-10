package auction.client.interfaces;

import java.net.Socket;

public interface INetworkSocket {

	public void shutDownSocket();
	public void sendMessageToNetwork( String message );
	public Socket getConnection();
	
}
