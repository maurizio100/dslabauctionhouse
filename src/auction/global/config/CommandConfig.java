package auction.global.config;

public class CommandConfig {

	private CommandConfig(){}
	
	/*--- General Command Settings ---*/
	public static final int POSCOMMAND = 0;
	public static final char COMMANDNOTIFIER = '!';
	public static final String ARGSEPARATOR = " ";
	
	/*-- login stuff -- */
	/*login*/
	public static final String LOGIN = "login";
	public static final int POSCLIENTNAME = 1;
	public static final int POSUDPPORT = 2;
	
	/*logout*/
	public static final String LOGOUT = "logout";
	/*loginreject*/
	public static final String LOGINREJECT = "loginreject";
	public static final int LOGINREJECTTOKENCOUNT = 2;
	public static final int POSLOGINREJECTMESSAGE = 1;
	/*-- auction stuff -- */
	/*list*/
	public static final String LIST = "list";
	/*create auction*/
	public static final String CREATEAUCTION = "create";
	public static final int CREATEAUCTIONTOKENCOUNT = 3;
	public static final int POSAUCTIONTIME = 1;
	public static final int POSAUCTIONDESCRIPTION = 2;
	/*bid auction*/
	public static final String BID = "bid";
	public static final String GROUPBID = "groupBid";
	public static final int BIDAUCTIONTOKENCOUNT = 3;
	public static final int POSAUCTIONNUMBER = 1;
	public static final int POSBID = 2;
//	public static final int POSGROUPBIDDER = 3;
	/*overbid*/
	public static final String OVERBID = "new-bid";
	public static final int OVERBIDTOKENCOUNT = 2;
	public static final int POSOVERBIDAUCTION = 1;
	/*auction-end*/
	public static final String AUCTIONENDED = "auction-ended";
	public static final int AUCTIONENDEDTOKENCOUNT = 4;
	public static final int POSAUCTIONWINNER = 1;
	public static final int POSAUCTIONPRICE = 2;
	public static final int POSAUCTIONENDDESCRIPTION = 3;
	/*confirm groupbid*/
	public static final String CONFIRM = "confirm";
	public static final int CONFIRMTOKENCOUNT = 4;
	public static final int POSCONFIRMAUCTIONNUMBER = 1;
	public static final int POSCONFIRMBID = 2;
	public static final int POSCONFIRMGROUPBIDDER = 3;
	
	/*confirmnotification*/
	public static final String CONFIRMED = "confirmed";

	/*-- handshake stuff --*/
	/*ok*/
	public static final String OK = "ok";
	public static final int POSCLIENTCHALLENGE = 1;
	public static final int POSSERVERCHALLENGE = 2;
	public static final int POSSECRETKEY = 3;
	public static final int POSIVPARAMETER = 4;
	/*reject*/
	public static final String REJECTED = "rejected";
	public static final int REJECTEDTOKENCOUNT = 3;
	public static final int POSREJECTMESSAGE = 1;
	
	/*-- exit --*/
	public static final String END = "end";
	
	
	
}
