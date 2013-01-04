package auction.server;

import auction.exceptions.ProductNotAvailableException;

public interface AuctionOperator {

	public void bidForAuction(int auctionNumber, ClientThread bidder, double bid) throws ProductNotAvailableException;
	public int addAuction(String description, ClientThread owner, int time);
	public void listAuction(ClientThread thread);
	public int getAuctionAmount();
	public boolean isAuctionIdAvailable( int auctionId );
	
}
