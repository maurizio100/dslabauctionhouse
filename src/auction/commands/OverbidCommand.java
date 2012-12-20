package auction.commands;

public class OverbidCommand implements Command {

	private AuctionCommandReceiverServer receiver = null;
	
	public OverbidCommand(AuctionCommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.overbid();
	}

	@Override
	public String getName() {
		return "new-bid";
	}

}
