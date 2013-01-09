package auction.client.interfaces;

public interface INetworkMessageReceiver {

	public void receiveNetworkMessage( String message );
	public void receiveNetworkInformation(String info);
	public void receiveNetworkStatusMessage(String message);
	public void receiveLogoutSignal();
}
