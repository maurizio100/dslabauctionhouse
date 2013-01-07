package auction.commands;

import auction.interfaces.IAuctionCommandReceiverServer;

public class OverbidCommand implements ICommand {

	private IAuctionCommandReceiverServer receiver = null;
	
	public OverbidCommand(IAuctionCommandReceiverServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.overbid();
	}

	@Override
	public String getName() {
		return "new-bid";
	}

}
