package auction.communication;

public interface MessageReceiver {

	public void receiveMessage( String message );
	public void invokeShutdown();
	
}
