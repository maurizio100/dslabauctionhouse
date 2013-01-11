package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class GetClientListCommand implements ICommand {

	private IAuctionCommandReceiver receiver = null;
	
	public GetClientListCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.getClientList();
	}

	@Override
	public String getName() {
		return CommandConfig.GETCLIENTLIST;
	}

}
