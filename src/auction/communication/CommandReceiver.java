package auction.communication;

import auction.server.Client;

public interface CommandReceiver {

	public void receiveCommand(String command, Client source);
	
}
