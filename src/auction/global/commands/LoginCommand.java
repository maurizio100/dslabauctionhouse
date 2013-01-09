package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class LoginCommand implements ICommand {

	private IAuctionCommandReceiver recepient = null;
	
	public LoginCommand(IAuctionCommandReceiver recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.login();
	}

	@Override
	public String getName() {
		return CommandConfig.LOGIN;
	}

}
