package auction.communication;

import java.util.ArrayList;

import auction.server.Client;

public class CommandController implements CommandReceiver, CommandSender{

	private ArrayList<CommandReceiver> commandReceiver = new ArrayList<CommandReceiver>();
	
	@Override
	public void registerCommandReceiver(CommandReceiver receiver) {
		commandReceiver.add(receiver);
	}

	@Override
	public void receiveCommand(String command, Client source) {
		for( CommandReceiver cr : commandReceiver ){
			cr.receiveCommand(command, source);
		}
		
	}

}
