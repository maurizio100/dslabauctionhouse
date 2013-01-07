package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;

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
