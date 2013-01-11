package auction.server.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;
import auction.server.interfaces.ICommandReceiverServer;

public class CloseCommand implements ICommand {

	private ICommandReceiverServer receiver = null;
	
	public CloseCommand(ICommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.close();
	}

	@Override
	public String getName() {
		return CommandConfig.CLOSE;
	}

}
