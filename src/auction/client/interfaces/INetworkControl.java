package auction.client.interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

public interface INetworkControl {

	public void sendMessageToNetwork( String message );
	public void shutDownNetworkConnection();
	public void registerNetworkMessageReceiver( INetworkMessageReceiver receiver );
	public boolean tryConnectToServer();
	public void exitClientSupportConnection();
	public void addSupportClient(String host, int port) throws UnknownHostException, IOException;
	public void sendToSupportClients(String message);
	
}

