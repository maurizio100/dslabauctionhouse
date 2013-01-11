package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class AuctionEndedCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public AuctionEndedCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.endAuction();
	}

	@Override
	public String getName() {
		return CommandConfig.AUCTIONENDED;
	}

}
