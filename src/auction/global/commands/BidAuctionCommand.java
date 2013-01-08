package auction.global.commands;

import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class BidAuctionCommand implements ICommand {

	private IAuctionCommandReceiver receiver = null;
	
	public BidAuctionCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.bidForAuction();
	}

	@Override
	public String getName() {
		return "bid";
	}

}
