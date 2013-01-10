package auction.client.interfaces;

import java.io.IOException;
import java.net.UnknownHostException;

public interface INetworkControl {

	public void sendMessageToNetwork( String message );
	public void shutDownNetworkConnection();
	public void registerNetworkMessageReceiver( INetworkMessageReceiver receiver );
	public boolean tryConnectToServer();
	public void exitClientSupportConnection();
	public void sendToSupportClients(String message);
	void addSupportClient(String host, int port, String servername)
			throws UnknownHostException, IOException;
	public boolean supportConnectionEstablished();
	
}

