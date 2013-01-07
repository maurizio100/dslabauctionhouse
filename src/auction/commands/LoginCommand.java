package auction.commands;

import auction.client.interfaces.IClientCommandReceiver;

public class LoginCommand implements ICommand {

	private IClientCommandReceiver recepient = null;
	
	public LoginCommand(IClientCommandReceiver recepient) {
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
