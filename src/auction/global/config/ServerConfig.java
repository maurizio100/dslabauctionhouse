package auction.global.config;

public class ServerConfig {

	private ServerConfig(){}
	
	public static final int GROUPBIDDELAY = 2500;
	public static final int GROUPBIDPERIOD = 5000;
	
	
	public static final String LOGINERROR = GlobalConfig.ERRORSTRING + " There was an error during Login procedure!";
	public static final String BIDNUMBERFORMATERROR = GlobalConfig.ERRORSTRING + " Couldn't bid for auction: auctionNumber and bid-number must be numeric.";
	public static final String AUCTIONNOTAVAILABLEERROR = GlobalConfig.ERRORSTRING + " The Auction you want to bid is not available.";
	public static final String GROUPBIDFULLERROR = GlobalConfig.ERRORSTRING + " There are already too much groupBids! The Bid will be set when it is possible!";
	public static final String USERLOGGEDINERROR = GlobalConfig.ERRORSTRING + " User already logged in!";
	public static final String PORTFORAMTERROR = GlobalConfig.ERRORSTRING + " Couldn't login: The udpPort must be numeric and digit between 1024 and 65535!";
	public static final String TIMEFORMATERROR = GlobalConfig.ERRORSTRING + " Couldn't create auction: The time must be numeric.";
	public static final String GROUPBIDAVAILABLEINFO = GlobalConfig.ERRORSTRING + " There is already an active groupbid for this auction!";
	
	/*login extras*/
	public static final int LOGINSERVERTOKENCOUNT = 4;
	public static final int POSLOGINCLIENTCHALLENGE = 3;
	
	public static final int SERVERCHALLENGESIZE = 32;
	public static final int IVPARAMSIZE = 16;
	
	public static final int CONFIRMLIMIT = 2;
	
	public static final String LOGOUTNOTIFICATON = "You are logged out now.";	
	public static final String SHUTDOWNNOTIFICATION = "Shutting down Server!";
	

	
}
