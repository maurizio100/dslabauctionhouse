package auction.commands;

public class LoginCommand implements Command {

	private ClientCommandReceiver recepient = null;
	
	public LoginCommand(ClientCommandReceiver recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.login();
	}

	@Override
	public String getName() {
		return "login";
	}

}
