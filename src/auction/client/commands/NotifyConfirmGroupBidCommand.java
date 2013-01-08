package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class NotifyConfirmGroupBidCommand implements ICommand {

	private ICommandReceiverClient receiver = null;
	
	public NotifyConfirmGroupBidCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.notifyConfirmed();
	}

	@Override
	public String getName() {
		return CommandConfig.CONFIRMED;
	}

}
