package auction.communication;

import auction.interfaces.IMessageReceiver;
import auction.interfaces.IMessageSender;


public class LocalMessageController implements IMessageReceiver, IMessageSender{

	private IMessageReceiver clientModel;
	
	@Override
	public void registerMessageReceiver(IMessageReceiver receiver) {
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

	@Override
	public void switchToOfflineMode() {
		clientModel.switchToOfflineMode();
		
	}

}
