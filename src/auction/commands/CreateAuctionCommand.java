package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;

public class CreateAuctionCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver = null;
	
	public CreateAuctionCommand(IAuctionCommandReceiverClient receiver) {
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
