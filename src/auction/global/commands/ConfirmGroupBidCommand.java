package auction.global.commands;

import auction.global.config.CommandConfig;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class ConfirmGroupBidCommand implements ICommand {

	private IAuctionCommandReceiver receiver = null;
	
	public ConfirmGroupBidCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.confirmGroupBid();
	}

	@Override
	public String getName() {
		return CommandConfig.CONFIRM;
	}

}
