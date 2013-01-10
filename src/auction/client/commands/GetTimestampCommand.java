package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class GetTimestampCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public GetTimestampCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.getTimeStamp();
	}

	@Override
	public String getName() {
		return CommandConfig.GETTIMESTAMP;
	}

}
