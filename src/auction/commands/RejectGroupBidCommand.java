package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;

public class RejectGroupBidCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver = null;
	
	public RejectGroupBidCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.rejectGroupBid();
	}

	@Override
	public String getName() {
		return "rejected";
	}

}
