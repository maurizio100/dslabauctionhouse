package auction.commands;

public class ConfirmGroupBidCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public ConfirmGroupBidCommand(AuctionCommandReceiverClient receiver) {
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
