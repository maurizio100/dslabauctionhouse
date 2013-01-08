package auction.server;

import java.util.HashMap;
import java.util.Timer;

import auction.global.exceptions.ProductNotAvailableException;
import auction.global.interfaces.ICommandReceiver;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.IFeedbackObserver;
import auction.interfaces.IAuctionEndReceiver;
import auction.server.interfaces.IAuctionOperator;
import auction.server.interfaces.IClientThread;

public class AuctionManager implements IAuctionOperator, IAuctionEndReceiver,
		IExitObserver {

	private HashMap<Integer, Auction> activeAuctions = null;
	private ICommandReceiver commandReceiver = null;
	private Timer timer;
	private int auctionId = 0;
	private IFeedbackObserver ifo = null;

	public AuctionManager(ICommandReceiver cr, IExitSender es,
			IFeedbackObserver ifo) {
		activeAuctions = new HashMap<Integer, Auction>();
		commandReceiver = cr;
		es.registerExitObserver(this);
		this.ifo = ifo;
		timer = new Timer();
	}

	@Override
	public void bidForAuction(int auctionNumber, IClientThread bidder,
			double price) throws ProductNotAvailableException {
		if (!activeAuctions.containsKey(auctionNumber)) {
			throw new ProductNotAvailableException();
		}

		Auction a = activeAuctions.get(auctionNumber);
		String lastBidder = null;
		String notification = null;
		boolean overbid = false;

		synchronized (a) {
			lastBidder = a.getLastBidder();
			notification = "!new-bid " + a.getDescription() + " " + lastBidder;
			overbid = a.setNewPrice(bidder.getClientName(), price);
		}

		if (overbid) {
			ifo.receiveFeedback("You have successfully bid with " + price
					+ " on " + a);
			if (lastBidder != null)
				this.notifyClientBidUpdate(notification);
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

		String notification = "!auction-ended " + winner + " "
				+ a.getHighestValue() + " " + a.getDescription() + " " + owner;
		commandReceiver.receiveCommand(notification, null);
		notification = "!auction-ended " + winner + " " + a.getHighestValue()
				+ " " + a.getDescription() + " " + winner;
		commandReceiver.receiveCommand(notification, null);

		this.removeAuction(a.getID());

	}

	private void removeAuction(int id) {

		synchronized (activeAuctions) {
			activeAuctions.remove(id);
		}

	}

	@Override
	public int addAuction(String description, IClientThread owner, int time) {

		description.replace("\n", "");
		auctionId++;
		Auction auc = new Auction(this, owner, description, time, auctionId);

		activeAuctions.put(auctionId, auc);
		
		timer.schedule(auc, 0, 500);

		return auctionId;

	}

	@Override
	public String listAuction(IClientThread thread) {
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

	public void receiveFeedback(String message)
	{
		ifo.receiveFeedback(message);
	}
}
