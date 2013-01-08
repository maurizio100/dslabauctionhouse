package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class OkCommand implements ICommand {

	private ICommandReceiverClient receiver;
	
	public OkCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.ok();
	}

	@Override
	public String getName() {
		return CommandConfig.OK;
	}

}
