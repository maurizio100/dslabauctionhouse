package auction.global.config;


public class ClientConfig {

	private ClientConfig(){}
	
	private static final String INFOCLIENT = Components.CLIENT + " " + GlobalConfig.INFOSTRING;
	private static final String ERRORCLIENT = Components.CLIENT + " " + GlobalConfig.ERRORSTRING;
	
	public static final String HMACNOTEQUAL = ERRORCLIENT + " " + "HMAC is not equal!";
	
	public static final String STILLLOGGEDIN = INFOCLIENT + " " + "You are still logged in!";
	public static final String STILLLOGGEDOUT = INFOCLIENT + " " + "You are still logged out!";
	public static final String LOGINSUCCESSFUL = INFOCLIENT + " " + "Login Successful!";
	public static final String LOGINFAILED = INFOCLIENT + " " + "Login Failed!";
	public static final String CREATENOTLOGGEDIN = INFOCLIENT + "Auction cant be created - You are not Logged in!";
	public static final String BIDNOTLOGGEDIN = INFOCLIENT + "You can\'t bid for the Auction - You are not Logged in!";
	public static final String CONFIRMLOGGEDOUT = INFOCLIENT + "You can\'t confirm to a groupbid - You are not Logged in!";
	public static final String GROUPBIDCONFIRMED = INFOCLIENT + "Groupbid confirmed!";
}
