package auction.client.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.config.CommandConfig;
import auction.global.interfaces.ICommand;

public class LoginRejectCommand implements ICommand {

	private ICommandReceiverClient receiver;
	
	public LoginRejectCommand(ICommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.rejectLogin();
	}

	@Override
	public String getName() {
		return CommandConfig.LOGINREJECT;
	}

}
