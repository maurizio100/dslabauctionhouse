package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

public class OkCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver;
	
	public OkCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.ok();
	}

	@Override
	public String getName() {
		return "ok";
	}

}
