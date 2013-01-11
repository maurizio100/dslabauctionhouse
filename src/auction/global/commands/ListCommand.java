package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class ListCommand implements ICommand {

	private IAuctionCommandReceiver receiver;
	
	public ListCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.list();
	}

	@Override
	public String getName() {
		return CommandConfig.LIST;
	}

}
