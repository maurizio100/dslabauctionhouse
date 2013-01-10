package auction.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import auction.client.interfaces.IClientMessageForwarder;
import auction.client.interfaces.INetworkControl;
import auction.client.interfaces.INetworkMessageReceiver;
import auction.client.interfaces.INetworkSocket;

public class NetworkMessageForwardingController 
implements IClientMessageForwarder, INetworkControl
{
	private INetworkSocket socketPort = null;
	private ArrayList<INetworkSocket> supportClients = null;
	private String host = null;
	private int tcpPort = -1;
	private INetworkMessageReceiver client = null;
	

	public NetworkMessageForwardingController(String host, int tcpPort) throws UnknownHostException, IOException{
		this.host = host;
		this.tcpPort = tcpPort;
		this.supportClients = new ArrayList<INetworkSocket>();
		
		socketPort = new ClientTCPPort( host, tcpPort, this );
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
	}

	@Override
	public boolean tryConnectToServer() {
		try {
			socketPort = new ClientTCPPort(host, tcpPort, this);
			return true;
		}catch (IOException e) {
			this.client.receiveNetworkStatusMessage("INFO: Server is still not reachable!");
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
	public void addSupportClient(String host, int port) throws UnknownHostException, IOException {
		supportClients.add( new ClientTCPPort(host, port, this));
	}

	@Override
	public void sendToSupportClients(String message) {
		for( INetworkSocket ns : supportClients ){
			ns.sendMessageToNetwork(message);
		}
	}
}
