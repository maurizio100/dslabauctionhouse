package auction.global.communication;

import auction.global.interfaces.ILocalMessageReceiver;
import auction.global.interfaces.ILocalMessageSender;


public class LocalMessageController
implements ILocalMessageReceiver, ILocalMessageSender{

	private ILocalMessageReceiver clientModel;
	
	@Override
	public void registerMessageReceiver(ILocalMessageReceiver receiver) {
		clientModel = receiver;
	}

	@Override
	public void receiveLocalMessage(String message) {
		clientModel.receiveLocalMessage(message);
	}


}
