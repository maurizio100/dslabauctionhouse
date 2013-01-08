package auction.global.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.interfaces.ICommand;

public class LoginCommand implements ICommand {

	private ICommandReceiverClient recepient = null;
	
	public LoginCommand(ICommandReceiverClient recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.login();
	}

	@Override
	public String getName() {
		return "login";
	}

}
