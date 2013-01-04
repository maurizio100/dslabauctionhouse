package auction.server;

public class GroupBid {

	private int auctionNumber;
	private double bid;
	private Client groupBidder;
	
	public GroupBid(int auctionNumber, double bid, Client servedClient) {
		this.auctionNumber = auctionNumber;
		this.bid = bid;
		this.groupBidder = servedClient;
	
	}

	public ClientThread getGroupBidder() {
		return groupBidder;
	}

	public String toString(){
		String retString = "";
		retString += "Auction: " + auctionNumber + "\n";
		retString += "Bid: " + bid;
		
		return retString;
	}
	
}
