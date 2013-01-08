package auction.server.commands;

import auction.global.interfaces.ICommand;
import auction.interfaces.IServerControl;

public class CloseCommand implements ICommand {

	private IServerControl receiver = null;
	
	public CloseCommand(IServerControl receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.close();
	}

	@Override
	public String getName() {
		return "close";
	}

}
