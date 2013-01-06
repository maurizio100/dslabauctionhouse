package auction.interfaces;

public interface IMessageReceiver {

	public void receiveMessage( String message );
	public void invokeShutdown();
	
}
