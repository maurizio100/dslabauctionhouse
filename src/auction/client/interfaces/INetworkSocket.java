package auction.client.interfaces;

public interface INetworkSocket {

	public void shutDownSocket();
	public void sendMessageToNetwork( String message );
	
}
