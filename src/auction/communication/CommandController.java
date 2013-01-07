package auction.communication;

import java.util.ArrayList;

import auction.commands.ICommandReceiver;
import auction.commands.ICommandSender;
import auction.server.Client;

public class CommandController implements ICommandReceiver, ICommandSender{

	private ArrayList<ICommandReceiver> commandReceiver = new ArrayList<ICommandReceiver>();
	
	@Override
	public void registerCommandReceiver(ICommandReceiver receiver) {
		commandReceiver.add(receiver);
	}

	@Override
	public void receiveCommand(String command, Client source) {
		for( ICommandReceiver cr : commandReceiver ){
			cr.receiveCommand(command, source);
		}
		
	}

}
