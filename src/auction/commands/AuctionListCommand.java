package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

public class AuctionListCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver;
	
	public AuctionListCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.ok();
	}

	@Override
	public String getName() {
		return "auctionlist";
	}

}
