package auction.communication;

public interface ExitSender {

	public void registerExitObserver(ExitObserver e);
	public void sendExit();
	
}
