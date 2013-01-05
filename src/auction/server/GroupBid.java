package auction.server;

import java.util.ArrayList;

public class GroupBid {

	private int auctionNumber;
	private double bid;
	private Client groupBidder;
	private ArrayList<Client> confirmClients;
	
	public GroupBid(int auctionNumber, double bid, Client servedClient) {
		this.auctionNumber = auctionNumber;
		this.bid = bid;
		this.groupBidder = servedClient;	
	}

	public int addConfirmClient( Client confClient ){
		confirmClients.add(confClient);
		return confirmClients.size();
	}
	
	public Client getGroupBidder() {
		return groupBidder;
	}

	public String toString(){
		String retString = "";
		retString += "Auction: " + auctionNumber + "\n";
		retString += "Bid: " + bid;
		
		return retString;
	}
	
	public ArrayList<Client> getConfirmClients(){
		return confirmClients;
	}

	public boolean isEqual(double bid) {
		return this.bid == bid;
	}

	public int getAuctionNumber() {
		return auctionNumber;
	}
	
	public double getBid(){
		return bid;
	}
	
	
}
