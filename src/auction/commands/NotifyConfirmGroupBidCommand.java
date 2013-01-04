package auction.commands;

public class NotifyConfirmGroupBidCommand implements Command {

	private AuctionCommandReceiverClient receiver = null;
	
	public NotifyConfirmGroupBidCommand(AuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.notifyConfirmed();
	}

	@Override
	public String getName() {
		return "confirmed";
	}

}
