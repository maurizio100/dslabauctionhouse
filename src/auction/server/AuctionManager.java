package auction.server;

import java.util.HashMap;
import java.util.Timer;

import auction.global.exceptions.ProductNotAvailableException;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.server.interfaces.IAuctionActivityReceiver;
import auction.server.interfaces.IAuctionMessageReceiver;
import auction.server.interfaces.IAuctionOperator;
import auction.server.interfaces.IClientThread;

public class AuctionManager 
implements IAuctionOperator, IAuctionMessageReceiver, IExitObserver {

	private HashMap<Integer, Auction> activeAuctions = null;
	private Timer timer;
	private int auctionId = 0;
	private IAuctionActivityReceiver auctionActivityReceiver = null;

	public AuctionManager(IExitSender es, IAuctionActivityReceiver activityReceiver) {
		activeAuctions = new HashMap<Integer, Auction>();
		timer = new Timer();
		
		this.auctionActivityReceiver = activityReceiver;
		es.registerExitObserver(this);
	}

	@Override
	public int addAuction(String description, String owner, int time) {

		description.replace("\n", "");
		auctionId++;
		Auction auc = new Auction(this, owner, description, time, auctionId);
		activeAuctions.put(auctionId, auc);
		
		timer.schedule(auc, 0, 500);
		return auctionId;
	}
	
	@Override
	public void bidForAuction(int auctionNumber, String bidder, double price) throws ProductNotAvailableException {
		if (!activeAuctions.containsKey(auctionNumber)) {
			throw new ProductNotAvailableException();
		}

		Auction a = activeAuctions.get(auctionNumber);
		String lastBidder = null;
		String auctionDescription = null;
		boolean overbid = false;

		synchronized (a) {
			lastBidder = a.getLastBidder();
			auctionDescription = a.getDescription();
			overbid = a.setNewPrice(bidder, price);
		}

		if (overbid) {
			auctionActivityReceiver.receiveAuctionNotification("You have successfully bid with " + price + " on " + a);
			if (lastBidder != null)
				auctionActivityReceiver.overbid( auctionDescription, lastBidder );
		}
	}

	@Override
	public String listAuction() {
		String auctionList = "there are no active auctions";
		if (!activeAuctions.isEmpty()) {

			auctionList = "";
			Auction a = null;
			for (int key : activeAuctions.keySet()) {
				a = activeAuctions.get(key);
				synchronized (a) {
					auctionList += key + ". " + a + "\n";
				}
				
			}
		}
		return auctionList;
	}


	@Override
	public void notifyAuctionEnded(int auctionNumber) {
		Auction a = activeAuctions.get(auctionNumber);
		/*
		String winner = a.getLastBidder();
		String owner = a.getOwner();

		String notification = "!auction-ended " + winner + " "
				+ a.getHighestValue() + " " + a.getDescription() + " " + owner;
		commandReceiver.receiveCommand(notification, null);
		notification = "!auction-ended " + winner + " " + a.getHighestValue()
				+ " " + a.getDescription() + " " + winner;
		commandReceiver.receiveCommand(notification, null);*/
		
		this.removeAuction(a.getID());
		auctionActivityReceiver.endAuction(a);

	}

	private void removeAuction(int id) {
		synchronized (activeAuctions) {
			activeAuctions.remove(id);
		}
	}

	@Override
	public void exit() {
		timer.cancel();
	}

	@Override
	public int getAuctionAmount() {
		return activeAuctions.size();
	}

	@Override
	public boolean isAuctionIdAvailable(int auctionId) {
		return activeAuctions.containsKey(auctionId);
	}

	@Override
	public void receiveAuctionCreateMessage(String message) {
		auctionActivityReceiver.receiveAuctionNotification(message);
	}

}
