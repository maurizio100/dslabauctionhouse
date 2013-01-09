package auction.server.interfaces;

import auction.server.Auction;

public interface IAuctionActivityReceiver {

	public void receiveAuctionNotification(String message);
	public void receiveAuctionNotification(String message, IClientThread client);
	public void endAuction(Auction auction);
	public void overbid(String auctionDescription, String bidderName );
	
}
