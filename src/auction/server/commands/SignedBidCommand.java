package auction.server.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;
import auction.server.interfaces.ICommandReceiverServer;

public class SignedBidCommand implements ICommand {

	private ICommandReceiverServer receiver = null;
	
	public SignedBidCommand(ICommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.signedBid();
	}

	@Override
	public String getName() {
		return CommandConfig.SIGNEDBID;
	}

}
