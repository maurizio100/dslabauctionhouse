package auction.communication;


public class LocalMessageController implements MessageReceiver, MessageSender{

	private MessageReceiver clientModel;
	
	@Override
	public void registerMessageReceiver(MessageReceiver receiver) {
		clientModel = receiver;
	}

	@Override
	public void receiveMessage(String message) {
		clientModel.receiveMessage(message);
	}

	@Override
	public void invokeShutdown() {
		clientModel.invokeShutdown();
	}

}
