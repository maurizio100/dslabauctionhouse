package auction.commands;

import auction.server.Client;

public interface ICommandReceiver {

	public void receiveCommand(String command, Client source);
	
}
