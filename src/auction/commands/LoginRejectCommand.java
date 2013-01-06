package auction.commands;

import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.IClientCommandReceiver;
import auction.interfaces.ICommand;

public class LoginRejectCommand implements ICommand {

	private IClientCommandReceiver receiver;
	
	public LoginRejectCommand(IClientCommandReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void execute() {
		receiver.rejectLogin();
	}

	@Override
	public String getName() {
		return "loginreject";
	}

}
