package auction.commands;

public class CreateAuctionCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public CreateAuctionCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.createAuction();
	}

	@Override
	public String getName() {
		return "create";
	}

}
