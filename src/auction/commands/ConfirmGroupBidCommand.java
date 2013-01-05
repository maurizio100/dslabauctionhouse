package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

public class ConfirmGroupBidCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver = null;
	
	public ConfirmGroupBidCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.confirmGroupBid();
	}

	@Override
	public String getName() {
		return "confirm";
	}

}
