package auction.communication;

public interface CommandSender {

	public void registerCommandReceiver( CommandReceiver receiver);
	
}
