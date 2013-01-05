package auction.interfaces;

import auction.exceptions.ProductNotAvailableException;

public interface IAuctionOperator {

	public void bidForAuction(int auctionNumber, IClientThread bidder, double bid) throws ProductNotAvailableException;
	public int addAuction(String description, IClientThread owner, int time);
	public void listAuction(IClientThread thread);
	public int getAuctionAmount();
	public boolean isAuctionIdAvailable( int auctionId );
	
}
