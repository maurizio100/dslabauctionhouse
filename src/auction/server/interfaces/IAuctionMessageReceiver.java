package auction.server.interfaces;

public interface IAuctionMessageReceiver {
	public void notifyAuctionEnded(int auctionNumber);
	public void receiveAuctionCreateMessage( String message );
}
