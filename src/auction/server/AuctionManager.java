package auction.server;

import java.util.HashMap;
import java.util.Timer;

import auction.communication.CommandReceiver;
import auction.communication.ExitObserver;
import auction.communication.ExitSender;

public class AuctionManager implements AuctionOperator, AuctionEndReceiver, ExitObserver{

	private HashMap<Integer, Auction> activeAuctions = null;
	private CommandReceiver commandReceiver = null;
	private Timer timer;
	private int auctionId = 0;
	
	public AuctionManager(CommandReceiver cr, ExitSender es){
		activeAuctions = new HashMap<Integer,Auction>();
		commandReceiver = cr;
		es.registerExitObserver(this);
		timer = new Timer();
	}
	
	@Override
	public void bidForAuction(int auctionNumber, ClientThread bidder, double price) {
		try{
			if( !activeAuctions.containsKey(auctionNumber) ){
				throw new ProductNotAvailableException();
			}
			Auction	a = activeAuctions.get(auctionNumber);
			String lastBidder = null;
			String notification = null;
			boolean overbid = false;
			
			synchronized( a ){
				lastBidder = a.getLastBidder();
				notification = "!new-bid " + a.getDescription() + " " + lastBidder;
				overbid = a.setNewPrice(bidder.getClientName(), price);
			}
			
			if(overbid){
				
				bidder.receiveFeedback("You have successfully bid with " + price + " on " + a);
				if( lastBidder != null ) this.notifyClientBidUpdate(notification);
			
			}
		
			
		}catch( ProductNotAvailableException pnae){
			bidder.receiveFeedback("The Auction you want to bid is not available.");
		}
		
	}

	private void notifyClientBidUpdate(String notification) {
		commandReceiver.receiveCommand(notification, null);
		
	}

	@Override
	public void notifyClientAuctionEnded(int auctionNumber) {
		Auction a = activeAuctions.get(auctionNumber);
		String winner = a.getLastBidder();
		String owner = a.getOwner();
		
		String notification = "!auction-ended " + winner + " " + a.getHighestValue() + " " + a.getDescription() + " " + owner;
		commandReceiver.receiveCommand(notification, null);
		notification = "!auction-ended " + winner + " " + a.getHighestValue() + " " + a.getDescription() + " " + winner;
		commandReceiver.receiveCommand(notification, null);
		
		
		this.removeAuction(a.getID());
		
	}

	private void removeAuction(int id){
	
		synchronized(activeAuctions){
			activeAuctions.remove(id);
		}
		
	}
	
	@Override
	public void addAuction(String description, ClientThread owner, int time) {
	
		description.replace("\n", "");
		auctionId++;
		Auction auc = new Auction( this, owner, description, time, auctionId );		

		activeAuctions.put( auctionId, auc );
		
			
		timer.schedule(auc, 0, 500);
		
	}

	@Override
	public void listAuction(ClientThread thread) {
		String auctionList = "there are no active auctions";
		if(!activeAuctions.isEmpty()){

			auctionList = "";
			Auction a = null;
			for( int key : activeAuctions.keySet()){
				a = activeAuctions.get(key);
			
				synchronized( a ){
					auctionList += key + ". " + a + "\n";	
				}
				
			}
		
		}
		
		thread.receiveFeedback(auctionList);
		
	}

	@Override
	public void exit() {
		timer.cancel();
	}

	@Override
	public int getAuctionAmount() {
		return activeAuctions.size();
	}
	
}
