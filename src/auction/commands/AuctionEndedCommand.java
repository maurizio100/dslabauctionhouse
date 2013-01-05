package auction.commands;

import auction.interfaces.IAuctionCommandReceiverServer;
import auction.interfaces.ICommand;

public class AuctionEndedCommand implements ICommand {

	private IAuctionCommandReceiverServer receiver = null;
	
	public AuctionEndedCommand(IAuctionCommandReceiverServer receiver) {
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
