package auction.commands;

public interface ICommandSender {

	public void registerCommandReceiver( ICommandReceiver receiver);
	
}
