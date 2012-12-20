package auction.server;

public interface AuctionOperator {

	public void bidForAuction(int auctionNumber, ClientThread bidder, double bid);
	public void addAuction(String description, ClientThread owner, int time);
	public void listAuction(ClientThread thread);
	
}
