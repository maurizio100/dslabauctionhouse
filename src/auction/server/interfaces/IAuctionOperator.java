package auction.server.interfaces;

import auction.global.exceptions.ProductNotAvailableException;

public interface IAuctionOperator {

	public void bidForAuction(int auctionNumber, String bidder, double bid) throws ProductNotAvailableException;
	public int addAuction(String description, String owner, int time);
	public String listAuction();
	public int getAuctionAmount();
	public boolean isAuctionIdAvailable( int auctionId );
	public void bidForEndedAuction(int auctionId, String clientName,
			double bid, long timestamp) throws ProductNotAvailableException;
	
}
