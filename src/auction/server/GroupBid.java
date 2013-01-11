package auction.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

import auction.global.config.ServerConfig;
import auction.server.interfaces.IClientThread;

public class GroupBid extends TimerTask{

	private int auctionNumber;
	private double bid;
	private IClientThread groupBidder;
	private ArrayList<IClientThread> confirmClients;
	private ServerModel serverModel;
	private long startTime = 0;

	public GroupBid(int auctionNumber, double bid, IClientThread servedClient, ServerModel serverModel) {
		this.auctionNumber = auctionNumber;
		this.bid = bid;
		this.groupBidder = servedClient;	
		confirmClients = new ArrayList<IClientThread>();
		this.serverModel = serverModel;
	}

	public int addConfirmClient( IClientThread confClient ){
		if( confirmClients.isEmpty() ){
			startTime = new Date().getTime();
		}
		confirmClients.add(confClient);
	
		return confirmClients.size();
	}

	public IClientThread getGroupBidder() {
		return groupBidder;
	}

	public String toString(){
		String retString = "";
		retString += "Auction: " + auctionNumber + "\n";
		retString += "Bid: " + bid;

		return retString;
	}

	public void run(){
		if(!confirmClients.isEmpty() && confirmClients.size() < ServerConfig.CONFIRMLIMIT)
		{
			if( (new Date().getTime() - startTime)/1000 >= 5 ){
				serverModel.sendTimoutReject(this);
				confirmClients.removeAll(confirmClients);
			}
		}
	}

	public ArrayList<IClientThread> getConfirmClients(){
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
