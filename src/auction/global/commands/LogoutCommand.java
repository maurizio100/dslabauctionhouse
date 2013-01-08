package auction.global.commands;

import auction.client.interfaces.ICommandReceiverClient;
import auction.global.interfaces.ICommand;

public class LogoutCommand implements ICommand {

	private ICommandReceiverClient recepient;
	
	public LogoutCommand(ICommandReceiverClient recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.logout();
	}

	@Override
	public String getName() {
		return "logout";
	}

}
