package auction.commands;

public class LogoutCommand implements Command {

	private ClientCommandReceiver recepient;
	
	public LogoutCommand(ClientCommandReceiver recepient) {
		this.recepient = recepient;
	}

	@Override
	public void execute() {
		recepient.logout();
	}

	@Override
	public String getName() {
		return "logout";
	}

}
