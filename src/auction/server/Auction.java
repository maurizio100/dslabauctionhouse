package auction.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimerTask;

public class Auction extends TimerTask{

	public static final String DATE_FORMAT_NOW = "dd.MM.yyyy HH:mm";

	private AuctionEndReceiver notificationReceiver = null;
	private String owner = null;
	private String highestBidder = "none";
	private String description = null;

	private double actualValue = 0.0;

	private GregorianCalendar endDate = null;
	private DateFormat df = new SimpleDateFormat(DATE_FORMAT_NOW);
	private int id;
	
	public Auction(AuctionManager manager, ClientThread owner, String description, int duration, int id){
		notificationReceiver = manager;
		this.description = description; 
		this.id = id;
		this.owner = owner.getClientName();
		
		endDate = new GregorianCalendar();
		endDate.add(Calendar.SECOND, duration);
		Date d = endDate.getTime();

		owner.receiveFeedback("An auction \'" +description+ "\' with id " + id + " has been created and will end on " +
				df.format(d));
	}
	
	public void run(){
		Date now = new Date();
		if( endDate.getTime().getTime() <= now.getTime()){
			notificationReceiver.notifyClientAuctionEnded(id);
			this.cancel();
		}
		
	}
	

	public boolean setNewPrice(String bidder, double price) {
		if( actualValue < price ){
			actualValue = price;
			highestBidder = bidder;
			return true;
		}
		return false;
	}

	public String getDescription(){
		return description;
	}

	public String toString(){
		String highestBidderOutput = "none";
		if( highestBidder != null ){ highestBidderOutput = highestBidder; }
		Date d = endDate.getTime();

		return description + " " + df.format(d) + " " + actualValue + " " + highestBidderOutput ;
	}

	public String getLastBidder() {
		return highestBidder;
	}
	
	public String getOwner(){
		return owner;
	}

	public int getID(){
		return id;
	}

	public double getHighestValue() {
		return actualValue;
	}
}
