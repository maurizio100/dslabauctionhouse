package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class TimeStampCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public TimeStampCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.processTimeStamp();
	}

	@Override
	public String getName() {
		return CommandConfig.TIMESTAMP;
	}

}
