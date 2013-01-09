package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class RejectGroupBidCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public RejectGroupBidCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.rejectGroupBid();
	}

	@Override
	public String getName() {
		return CommandConfig.REJECTED;
	}

}
