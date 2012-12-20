package auction.commands;

public class BidAuctionCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public BidAuctionCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.bidForAuction();
	}

	@Override
	public String getName() {
		return "bid";
	}

}
