package auction.commands;

public class OkCommand implements Command {

	private AuctionCommandReceiverClient receiver;
	
	public OkCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.ok();
	}

	@Override
	public String getName() {
		return "ok";
	}

}
