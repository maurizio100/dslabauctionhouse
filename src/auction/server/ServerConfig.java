package auction.server;

public class ServerConfig {

	private ServerConfig(){}
	
	public static final int POSCOMMAND = 0;
	public static final int POSCLIENTNAME = 1;
	public static final int POSUDPPORT = 2;
	
	public static final int POSCLIENTCHALLENGE = 3;
	
	public static final int POSAUCTIONTIME = 1;
	public static final int POSAUCTIONNUMBER = 1;
	public static final int POSBID = 2;
	public static final int POSGROUPBIDDER = 3;
	
	public static final int SERVERCHALLENGESIZE = 32;
	public static final int IVPARAMSIZE = 16;
	
	public static final int CONFIRMLIMIT = 2;
	
	public static final char COMMANDNOTIFIER = '!';
	public static final String OKCOMMAND = COMMANDNOTIFIER + "ok";
	public static final String GROUPBIDCOMMAND = COMMANDNOTIFIER + "groupBid";
	public static final String REJECTCOMMAND = COMMANDNOTIFIER + "reject";
	public static final String ARGSEPARATOR = " ";

	public static final String PUBLICKEYFILEPOSTFIX = ".pub.pem";
	
	public static final String LOGOUTNOTIFICATON = "You are logged out now.";	
	public static final String SHUTDOWNNOTIFICATION = "Shutting down Server!";
	
	public static final String BIDNUMBERFORMAtERROR = "Couldn't bid for auction: auctionNumber and bid-number must be numeric.";
	public static final String AUCTIONNOTAVAILABLEERROR = "The Auction you want to bid is not available.";
	public static final String GROUPBIDFULLERROR = "There are already too much groupBids! The Bid will be set when it is possible!";
	public static final String USERLOGGEDINERROR = "User already logged in!";
	public static final String PORTFORAMTERROR = "Couldn't login: The udpPort must be numeric and digit between 1024 and 65535!";
	public static final String TIMEFORMATERROR = "Couldn't create auction: The time must be numeric.";
	
}
