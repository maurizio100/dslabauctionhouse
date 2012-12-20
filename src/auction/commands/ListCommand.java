package auction.commands;

public class ListCommand implements Command {

	private AuctionCommandReceiverClient receiver;
	
	public ListCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.list();
	}

	@Override
	public String getName() {
		return "list";
	}

}
