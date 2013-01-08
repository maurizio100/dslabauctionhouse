package auction.client.interfaces;

public interface INetworkControl {

	public void sendMessageToNetwork( String message );
	public void shutDownNetworkConnection();
	public void registerNetworkMessageReceiver( INetworkMessageReceiver receiver );
}

