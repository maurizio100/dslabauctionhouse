package auction.commands;

import auction.client.interfaces.IAuctionCommandReceiverClient;
import auction.client.interfaces.IClientCommandReceiver;

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
