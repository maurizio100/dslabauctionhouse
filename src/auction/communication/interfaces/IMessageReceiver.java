package auction.communication.interfaces;

public interface IMessageReceiver {

	public void receiveMessage( String message );
	public void invokeShutdown();
	public void switchToOfflineMode();
	
}
