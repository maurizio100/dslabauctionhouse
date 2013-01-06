package auction.communication;

import auction.interfaces.IMessageReceiver;
import auction.interfaces.IMessageSender;


public class NetworkMessageForwardingController implements IMessageSender, IMessageReceiver{

	private IMessageReceiver socketPort = null;
	

	@Override
	public void receiveMessage(String message) {
		socketPort.receiveMessage(message);
	}

	@Override
	public void registerMessageReceiver(IMessageReceiver receiver) {
		socketPort = receiver;
	}

	@Override
	public void invokeShutdown() {
		socketPort.invokeShutdown();
	}

	@Override
	public void switchToOfflineMode() {
		socketPort.switchToOfflineMode();
		
	}

}
