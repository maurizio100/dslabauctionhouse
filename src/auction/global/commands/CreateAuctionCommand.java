package auction.global.commands;

import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;

public class CreateAuctionCommand implements ICommand {

	private IAuctionCommandReceiver receiver = null;
	
	public CreateAuctionCommand(IAuctionCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.createAuction();
	}

	@Override
	public String getName() {
		return "create";
	}

}
