package auction.server.commands;

import auction.global.interfaces.ICommand;
import auction.interfaces.IServerControl;

public class ReconnectCommand implements ICommand {

	private IServerControl receiver = null;
	
	public ReconnectCommand(IServerControl receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.reconnect();
	}

	@Override
	public String getName() {
		return "reconnect";
	}

}