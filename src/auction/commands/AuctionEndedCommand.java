package auction.commands;

public class AuctionEndedCommand implements Command {

	private AuctionCommandReceiverServer receiver = null;
	
	public AuctionEndedCommand(AuctionCommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.endAuction();
	}

	@Override
	public String getName() {
		return "auction-ended";
	}

}
