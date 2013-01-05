package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

public class GroupBidAuctionCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver = null;
	
	public GroupBidAuctionCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.bidForAuction();
	}

	@Override
	public String getName() {
		return "groupBid";
	}

}
