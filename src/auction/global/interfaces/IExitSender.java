package auction.global.interfaces;

public interface IExitSender {

	public void registerExitObserver(IExitObserver e);
	public void sendExit();
	
}
