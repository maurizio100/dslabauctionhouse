package auction.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimerTask;

import auction.client.interfaces.IClientMessageForwarder;
import auction.client.interfaces.INetworkControl;
import auction.client.interfaces.INetworkMessageReceiver;
import auction.client.interfaces.INetworkSocket;

public class NetworkMessageForwardingController extends TimerTask
implements IClientMessageForwarder, INetworkControl
{
	private INetworkSocket socketPort = null;
	private ArrayList<INetworkSocket> supportClients = null;
	private String host = null;
	private int tcpPort = -1;
	private INetworkMessageReceiver client = null;
	private boolean serverOnline = true;


	public NetworkMessageForwardingController(String host, int tcpPort) throws UnknownHostException, IOException{
		this.host = host;
		this.tcpPort = tcpPort;
		this.supportClients = new ArrayList<INetworkSocket>();

		socketPort = new ClientTCPPort( host, tcpPort, this, true );
	}

	public NetworkMessageForwardingController( String host, int tcpPort, int udpPort) throws UnknownHostException, IOException{
		this( host, tcpPort );
		//		ClientUDPPort updPort = new ClientUDPPort( udpPort );

	}

	@Override
	public void registerNetworkMessageReceiver(INetworkMessageReceiver receiver) {
		this.client = receiver;
	}

	@Override
	public void sendMessageToNetwork(String message) {
		if( socketPort != null ){
			socketPort.sendMessageToNetwork(message);
		}	
	}

	@Override
	public void sendMessageToClient(String message) {
		client.receiveNetworkMessage(message);
	}

	@Override
	public void shutDownNetworkConnection() {
		if( socketPort != null ){
			socketPort.shutDownSocket();
		}
	}

	@Override
	public void sendNetworkInformationToClient(String info) {
		this.client.receiveNetworkInformation( info );

	}

	@Override
	public void sendNetworkStatusMessageToClient(String message) {
		this.client.receiveNetworkStatusMessage( message );
	}

	@Override
	public void sendDisconnectedSignal() {
		this.client.receiveDisconnectSignal();
		socketPort = null;
		serverOnline = false;
	}

	@Override
	public boolean tryConnectToServer() {
		try {
			
			socketPort = new ClientTCPPort(host, tcpPort, this, true);
			return true;
		}catch (IOException e) {
			socketPort = null;
		}
		return false;
	}

	@Override
	public void exitClientSupportConnection() {
		for( INetworkSocket ns : supportClients ){
			ns.shutDownSocket();
		}
		supportClients.removeAll(supportClients);
	}

	@Override
	public void addSupportClient(String host, int port, String servername) throws UnknownHostException, IOException {
		supportClients.add( new ClientTCPPort(host, port, this, false, servername));
	}

	@Override
	public void sendToSupportClients(String message) {
		for( INetworkSocket ns : supportClients ){
			ns.sendMessageToNetwork(message);
		}
	}

	@Override
	public void sendMessageToClient(String message, String servername) {
		this.client.receiveNetworkMessage(message, servername);

	}

	@Override
	public boolean supportConnectionEstablished() {
		return (supportClients.size() == 2);
	}

	@Override
	public void run() {
		if(!serverOnline)
		{
			if(tryConnectToServer())
			{
				serverOnline = true;
				client.reconnectToServer();
			}
		}
	}

}
