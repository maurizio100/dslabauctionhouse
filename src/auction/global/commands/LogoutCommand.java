package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class LogoutCommand implements ICommand {

	private IAuctionCommandReceiver recepient;
	
	public LogoutCommand(IAuctionCommandReceiver recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.logout();
	}

	@Override
	public String getName() {
		return CommandConfig.LOGOUT;
	}

}
