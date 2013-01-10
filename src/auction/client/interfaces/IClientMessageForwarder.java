package auction.client.interfaces;

public interface IClientMessageForwarder {

	public void sendMessageToClient(String message);
	public void sendNetworkInformationToClient( String info );
	public void sendNetworkStatusMessageToClient(String message);
	public void sendDisconnectedSignal();
}
