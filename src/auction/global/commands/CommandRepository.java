package auction.global.commands;

import java.util.HashMap;

import auction.global.interfaces.ICommand;


public class CommandRepository {

	private HashMap<String, ICommand> repository = null;
	
	private CommandRepository(){
		repository = new HashMap<String, ICommand>();
	}
	
	public CommandRepository(ICommand command){
		this();
		createRepository(command);
	}
	
	public CommandRepository( ICommand[] commandList ){
		this();
		createRepository(commandList);
	}
	
	private void createRepository(ICommand command){
		repository.put(command.getName(), command);
	}
	
	private void createRepository(ICommand[] command){
		for( ICommand c : command ){
			createRepository(c);
		}
	}
	
	public ICommand checkCommand(String command){
		String[] splittedString = command.split("!");
		return repository.get(splittedString[1]);
	}

}
