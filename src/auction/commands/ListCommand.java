package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.ICommand;

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
