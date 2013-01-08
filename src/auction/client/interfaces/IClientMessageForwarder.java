package auction.client.interfaces;

public interface IClientMessageForwarder {

	public void sendMessageToClient(String message);
	public void sendNetworkInformationToClient( String info );
}
