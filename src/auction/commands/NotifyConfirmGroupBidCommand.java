package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

public class NotifyConfirmGroupBidCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver = null;
	
	public NotifyConfirmGroupBidCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.notifyConfirmed();
	}

	@Override
	public String getName() {
		return "confirmed";
	}

}
