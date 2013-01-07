package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;

public class ExitCommand implements ICommand{

private IAuctionCommandReceiverClient receiver;
	
	public ExitCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.exit();
	}

	@Override
	public String getName() {
		return "end";
	}


}
