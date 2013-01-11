package auction.server.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;
import auction.server.interfaces.ICommandReceiverServer;

public class ReconnectCommand implements ICommand{

private ICommandReceiverServer receiver;
	
	public ReconnectCommand(ICommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.reconnect();
	}

	@Override
	public String getName() {
		return CommandConfig.RECONNECT;
	}


}
