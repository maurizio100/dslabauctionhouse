package auction.commands;

public class RejectGroupBidCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public RejectGroupBidCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.rejectGroupBid();
	}

	@Override
	public String getName() {
		return "reject";
	}

}
