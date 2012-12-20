package auction.commands;

public class ExitCommand implements Command{

private AuctionCommandReceiverClient receiver;
	
	public ExitCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.exit();
	}

	@Override
	public String getName() {
		return "end";
	}


}
