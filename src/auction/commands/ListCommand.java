package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;

public class ListCommand implements ICommand {

	private IAuctionCommandReceiverClient receiver;
	
	public ListCommand(IAuctionCommandReceiverClient receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.list();
	}

	@Override
	public String getName() {
		return "list";
	}

}
