package auction.commands;

import auction.client.interfaces.IClientCommandReceiver;

public class LogoutCommand implements ICommand {

	private IClientCommandReceiver recepient;
	
	public LogoutCommand(IClientCommandReceiver recepient) {
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
