package auction.client;

import java.io.IOException;
import java.net.UnknownHostException;

import auction.client.interfaces.IClientMessageForwarder;
import auction.client.interfaces.INetworkControl;
import auction.client.interfaces.INetworkMessageReceiver;
import auction.client.interfaces.INetworkSocket;

public class NetworkMessageForwardingController 
implements IClientMessageForwarder, INetworkControl
{
	private INetworkSocket socketPort = null;
	private String host = null;
	private int tcpPort = -1;
	private INetworkMessageReceiver client = null;
	

	public NetworkMessageForwardingController(String host, int tcpPort) throws UnknownHostException, IOException{
		this.host = host;
		this.tcpPort = tcpPort;
		
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
		socketPort.sendMessageToNetwork(message);	
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


}
