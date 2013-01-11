package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class OverbidCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public OverbidCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.overbid();
	}

	@Override
	public String getName() {
		return CommandConfig.OVERBID;
	}

}
