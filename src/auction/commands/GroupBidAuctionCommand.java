package auction.commands;

public class GroupBidAuctionCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public GroupBidAuctionCommand(AuctionCommandReceiverClient receiver) {
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
