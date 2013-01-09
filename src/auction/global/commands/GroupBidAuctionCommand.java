package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class GroupBidAuctionCommand implements ICommand {

	private IAuctionCommandReceiver receiver = null;
	
	public GroupBidAuctionCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.bidForAuction();
	}

	@Override
	public String getName() {
		return CommandConfig.GROUPBID;
	}

}
