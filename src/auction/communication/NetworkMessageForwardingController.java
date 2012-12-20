package auction.communication;


public class NetworkMessageForwardingController implements MessageSender, MessageReceiver{

	private MessageReceiver socketPort = null;
	

	@Override
	public void receiveMessage(String message) {
		socketPort.receiveMessage(message);
	}

	@Override
	public void registerMessageReceiver(MessageReceiver receiver) {
		socketPort = receiver;
	}

	@Override
	public void invokeShutdown() {
		socketPort.invokeShutdown();
	}

}
