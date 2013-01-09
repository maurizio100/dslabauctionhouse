package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class ExitCommand implements ICommand{

private IAuctionCommandReceiver receiver;
	
	public ExitCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.exit();
	}

	@Override
	public String getName() {
		return CommandConfig.END;
	}


}
