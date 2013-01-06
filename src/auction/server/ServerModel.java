package auction.server;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;

import auction.commands.AuctionEndedCommand;
import auction.commands.BidAuctionCommand;
import auction.commands.CommandRepository;
import auction.commands.ConfirmGroupBidCommand;
import auction.commands.CreateAuctionCommand;
import auction.commands.ExitCommand;
import auction.commands.GroupBidAuctionCommand;
import auction.commands.ListCommand;
import auction.commands.LoginCommand;
import auction.commands.LogoutCommand;
import auction.commands.OverbidCommand;
import auction.crypt.AESCrypt;
import auction.crypt.HMAC;
import auction.crypt.RSACrypt;
import auction.exceptions.BidTooLowException;
import auction.exceptions.BidderNotAvailableException;
import auction.exceptions.BidderSameConfirmException;
import auction.exceptions.ProductNotAvailableException;
import auction.interfaces.IAuctionCommandReceiverServer;
import auction.interfaces.IAuctionOperator;
import auction.interfaces.IClientCommandReceiver;
import auction.interfaces.IClientOperator;
import auction.interfaces.IClientThread;
import auction.interfaces.ICommand;
import auction.interfaces.ICommandReceiver;
import auction.interfaces.ICommandSender;
import auction.interfaces.ICrypt;
import auction.interfaces.IExitObserver;
import auction.interfaces.IExitSender;
import auction.interfaces.IFeedbackObserver;
import auction.interfaces.IMessageReceiver;
import auction.interfaces.IMessageSender;
import auction.interfaces.IOInstructionReceiver;
import auction.interfaces.IOInstructionSender;
import auction.io.IOUnit;

public class ServerModel 
implements IExitSender, IAuctionCommandReceiverServer, IClientCommandReceiver, ICommandReceiver,IMessageReceiver, IOInstructionSender, IFeedbackObserver{

	private ArrayList<IExitObserver> eObservers = null;
	private IOInstructionReceiver ioReceiver = null;
	private Timer timer = null;

	/* --- Auction and Client Manager --------------- */
	private IAuctionOperator auctionManager = null; 
	private IClientOperator clientManager = null;

	/* ---- Cryptography variables ------------------ */
	private HashMap<String, ICrypt> cryptuser = null;
	private ICrypt crypt = null;
	private String pathToPublicKey = null;
	private PrivateKey privateKey = null;
	private HashMap<String, byte[]> secureNumberUser = new HashMap<String, byte[]>();
	private String pathToDir = null;

	/* ---- GroupBid Management --------------------- */
	private HashMap<Integer, GroupBid> groupBids = null;
	private GroupBidQueue queuedGroupBids = null; 

	/* ---- Command variables ----------------------- */
	private Client servedClient = null;

	private CommandRepository commandRepository = null;
	private String currentCommand = "";
	private String[] splittedString;

	private ICommand[] availableCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new AuctionEndedCommand(this),
			new OverbidCommand(this),
			new GroupBidAuctionCommand(this),
			new ConfirmGroupBidCommand(this)
	};

	/* ---- Constructors ---------------------------- */
	public ServerModel(IMessageSender lmc,
			ICommandSender cc, IClientOperator clientManager) {

		eObservers = new ArrayList<IExitObserver>();		
		groupBids = new HashMap<Integer,GroupBid>();

		commandRepository = new CommandRepository(availableCommands);
		this.clientManager = clientManager;
		queuedGroupBids = new GroupBidQueue();

		auctionManager = new AuctionManager(this,this, this);
		lmc.registerMessageReceiver(this);
		cc.registerCommandReceiver(this);
		timer = new Timer();
	}

	public ServerModel(IMessageSender lmc,
			ICommandSender cc, IClientOperator clientManager, String pathToPublicKey,PrivateKey privateKey, String pathToDir) {

		this(lmc, cc, clientManager);
		this.privateKey = privateKey;
		this.pathToPublicKey = pathToPublicKey;
		this.pathToDir = pathToDir;
		//server-challenge erstellen und in Base64 umwandeln wegen leerzeichen

		this.cryptuser = new HashMap<String, ICrypt>();
		crypt = new RSACrypt(privateKey);
	}

	/* -------------- Message parsing and processing -------------------- */
	@Override
	public void receiveMessage(String message) {
		if(message.length() == 0){
			this.sendToIOUnit(ServerConfig.SHUTDOWNNOTIFICATION);
			sendExit();
		}
		this.sendToIOUnit(message);
	}

	@Override
	public synchronized void receiveCommand(String command, Client source) {
		servedClient = source;
		this.parseMessage(command);
		servedClient = null;
	}

	private void parseMessage(String message) {
		ICommand c = null;
		if( this.isCommand(message) ){
			c = parseCommand(message);
			currentCommand = message;
		}
		else
		{
			if(cryptuser.containsKey(servedClient.getClientName()))
			{
				message = cryptuser.get(servedClient.getClientName()).decodeMessage(message);			}
			else
			{
				message = crypt.decodeMessage(message);
			}
			if( this.isCommand(message) ){
				c = parseCommand(message);
				currentCommand = message;
			}
			else
			{
				checkClientServerChallenge(message.getBytes());
			}
		}
		if(c != null)
		{
			c.execute();
			currentCommand = null;
		}

	}

	private ICommand parseCommand(String command){
		splittedString = command.split(ServerConfig.ARGSEPARATOR);
		String extractedCommand = splittedString[ServerConfig.POSCOMMAND];
		ICommand c = commandRepository.checkCommand(extractedCommand);
		return c;
	}

	private boolean isCommand(String message) { return message.charAt(0) == ServerConfig.COMMANDNOTIFIER; }

	private void sendGroupBidNotification(GroupBid groupBid) {
		Client groupBidder = groupBid.getGroupBidder();

		for( IClientThread ct : clientManager.getLoggedInClients() ){
			if( ct != groupBidder ){
				this.sendFeedback(ct, groupBidder.getClientName() + " has started the following group bid and needs two confirmations\n" + groupBid.toString());
			}
		}

	}

	/* -------- Login Management ------------------------- */
	@Override
	public void login() {
		try{

			String clientName = splittedString[ServerConfig.POSCLIENTNAME];
			int udpPort = Integer.parseInt(splittedString[ServerConfig.POSUDPPORT]);

			String userPublicKey = pathToDir + clientName + ServerConfig.PUBLICKEYFILEPOSTFIX;

			/* Response via TCP */
			if( !cryptuser.containsKey(clientName) )
			{
				//TODO Rethink error handling here
				clientManager.loginClient(clientName, udpPort, servedClient);
				try {
					cryptuser.put(clientName, new RSACrypt(userPublicKey, privateKey));
				} catch (IOException e) {
					e.printStackTrace();
				}

				/* every client gets a new server challenge */
				byte[] number = new byte[ServerConfig.SERVERCHALLENGESIZE];
				new SecureRandom().nextBytes(number);
				number = Base64.encode(number);			
				secureNumberUser.put(clientName, number);

				/* for IV-Param */
				byte[] iv = new byte[ServerConfig.IVPARAMSIZE];
				new SecureRandom().nextBytes(iv);
				iv = Base64.encode(iv);

				KeyGenerator generator;
				SecretKey key = null;

				//TODO Rethinking error handling here
				try {
					generator = KeyGenerator.getInstance("AES");
					generator.init(256); 
					key = generator.generateKey(); 

				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String secretkey = new String(Base64.encode(key.getEncoded()));
				String serverchallenge = new String(secureNumberUser.get(clientName));
				String ivparam = new String(iv);
				String clientchallenge = splittedString[ServerConfig.POSCLIENTCHALLENGE];



				String cryptmessage = ServerConfig.OKCOMMAND + " " + clientchallenge + " " + serverchallenge + " " + secretkey + " " + ivparam;

				this.sendFeedback(cryptmessage);

				cryptuser.put(clientName, new AESCrypt(key, iv));
			}
			else
			{
				this.sendLoginRject(ServerConfig.USERLOGGEDINERROR, clientName);
			}

		}catch(NumberFormatException nfe){
			this.sendFeedback(ServerConfig.PORTFORAMTERROR);
		} 
	}

	@Override
	public void logout() {
		clientManager.logoffClient(servedClient);
		this.sendFeedback(ServerConfig.LOGOUTNOTIFICATON);
		cryptuser.remove(servedClient.getClientName());
	}

	private void checkClientServerChallenge(byte[] number)
	{

		String n = new String(number);
		String cn = new String(secureNumberUser.get(servedClient.getClientName()));
		if(n.equals(cn))
		{
			sendToIOUnit("login successful!");		
		}
		else
		{
			//TODO Exception
		}
	}


	/* ---------- Auction Management ------------------------- */	
	@Override
	public void createAuction() {
		try{
			int time = Integer.parseInt(splittedString[ServerConfig.POSAUCTIONTIME]);
			String description = "";

			int i = 2;

			for (; i < (splittedString.length-1); i++ ){
				description += splittedString[i];
				description += " ";
			}
			description += splittedString[i];

			int auctionId = auctionManager.addAuction(description, servedClient, time);

		}catch(NumberFormatException nfe){this.sendFeedback(ServerConfig.TIMEFORMATERROR);}
	}

	@Override
	public void bidForAuction() {
		try{
			int auctionNumber = Integer.parseInt(splittedString[ServerConfig.POSAUCTIONNUMBER]);
			double bid = Double.parseDouble(splittedString[ServerConfig.POSBID]);

			if( !auctionManager.isAuctionIdAvailable(auctionNumber)){ throw new ProductNotAvailableException(); }

			/* --- GroupBid part ---------------- */
			if( splittedString[ServerConfig.POSCOMMAND].equals(ServerConfig.GROUPBIDCOMMAND)){
				GroupBid gb = new GroupBid(auctionNumber,bid, servedClient, this);
				this.addGroupBid(gb);
			}else{ auctionManager.bidForAuction(auctionNumber, servedClient, bid); }

		}catch( NumberFormatException nfe ){
			this.sendFeedback(ServerConfig.BIDNUMBERFORMAtERROR);
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback(ServerConfig.AUCTIONNOTAVAILABLEERROR);
		}
	}

	private void addGroupBid(GroupBid gb) {
		if( groupBids.size() < cryptuser.size() ){			

			if( !groupBids.containsKey( gb.getAuctionNumber() ) ){
				groupBids.put( gb.getAuctionNumber(), gb);
				timer.schedule(gb, 2500, 5000);
				this.sendGroupBidNotification(gb);

			}else{
				sendFeedback(gb.getGroupBidder(), ServerConfig.GROUPBIDAVAILABLEINFO);
				if( !queuedGroupBids.isEmpty() ){
					this.addGroupBid(queuedGroupBids.dequeue());
				}
			}
		}else{
			this.sendFeedback( ServerConfig.GROUPBIDFULLERROR );
			queuedGroupBids.enqueue(gb);
		}

	}

	@Override
	public void list() {
		String message = auctionManager.listAuction(servedClient);			
		this.sendFeedback(message);
	}

	@Override
	public void overbid() {
		String notification = splittedString[0] + ServerConfig.ARGSEPARATOR;
		int i = 1;
		for( ; i < splittedString.length-1; i++ ){ notification += splittedString[i]; }

		String lastBidder = splittedString[i];
		clientManager.sendNotification(notification, lastBidder);
	}

	@Override
	public void endAuction() {
		String notification = splittedString[0] + ServerConfig.ARGSEPARATOR + splittedString[1] + ServerConfig.ARGSEPARATOR + splittedString[2] + ServerConfig.ARGSEPARATOR;
		int i = 3;
		for( ; i < splittedString.length-1; i++ ){ notification += splittedString[i]; }

		String receiver = splittedString[i];
		clientManager.sendNotification(notification, receiver);
	}
	/* --------- GroupBid management -------------------------------- */
	@Override
	public void confirmGroupBid() {
		try{
			int auctionNumber = Integer.parseInt(splittedString[ServerConfig.POSAUCTIONNUMBER]);
			double bid = Double.parseDouble(splittedString[ServerConfig.POSBID]);
			String groupBidder = splittedString[ServerConfig.POSGROUPBIDDER].toLowerCase();

			if( !groupBids.containsKey(auctionNumber) ){
				throw new ProductNotAvailableException();
			}

			GroupBid gb = groupBids.get(auctionNumber);
			if( gb.getGroupBidder().getClientName().equals(servedClient.getClientName())){
				throw new BidderSameConfirmException();
			}

			if( !gb.getGroupBidder().getClientName().equals(groupBidder) ){
				throw new BidderNotAvailableException();
			}

			if( !gb.isEqual( bid ) ){
				throw new BidTooLowException();
			}

			if( gb.addConfirmClient(servedClient) == ServerConfig.CONFIRMLIMIT){
				gb.cancel();
				confirmBid(gb);
				notifyClients(gb);
				groupBids.remove(auctionNumber);

				if( !queuedGroupBids.isEmpty() ){
					GroupBid lastQueuedBid = queuedGroupBids.dequeue();
					this.addGroupBid(lastQueuedBid);
				}
			}else{
				//TODO Confirmnotification message
			}

		}catch( NumberFormatException nfe ){
			this.sendFeedback(ServerConfig.REJECTCOMMAND + " Couldn't confirm groupAuction: auctionNumber and bid must be numeric!" );
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback(ServerConfig.REJECTCOMMAND + " The AuctionId you want to confirm to is not available!");
		}catch( BidderNotAvailableException bnae){
			this.sendFeedback(ServerConfig.REJECTCOMMAND + " The Client you want to confirm has not bid to an auction with the given ID!");
		}catch( BidTooLowException btle ){
			this.sendFeedback(ServerConfig.REJECTCOMMAND + " Your bid is not equal to that of the group\'s bidder!");
		} catch (BidderSameConfirmException e) {
			this.sendFeedback(ServerConfig.REJECTCOMMAND + " You have set the GroupBid!");
		}

	}

	private void notifyClients(GroupBid gb) {
		ArrayList<Client> confirmers = gb.getConfirmClients();
		for( Client c : confirmers ){
			this.sendFeedback(c, "!confirmed");
		}
	}

	private void confirmBid(GroupBid gb) {
		int auctionNumber = gb.getAuctionNumber();
		double bid = gb.getBid();
		Client groupBidder = gb.getGroupBidder();
		try{
			servedClient = groupBidder;
			auctionManager.bidForAuction(auctionNumber, groupBidder, bid);
		}catch( ProductNotAvailableException pnae ){
			this.sendFeedback(groupBidder, "The product you want to bid is not available anymore!");
		}
	}


	/* ---------- Communication --------------- */
	private void sendLoginRject(String message, String clientName){
		message = "!loginreject" + ServerConfig.ARGSEPARATOR + message;
		try {
			String userPublicKey = pathToDir + clientName + ServerConfig.PUBLICKEYFILEPOSTFIX;
			message = new RSACrypt(userPublicKey,privateKey).encodeMessage(message);
			clientManager.sendFeedback(servedClient, message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	private void sendFeedback( String message ){ this.sendFeedback(servedClient,message); }

	private void sendFeedback(IClientThread c, String message ){
		//Encryption of feedback messages
		try
		{
			if(cryptuser.get(c.getClientName()) != null)
			{
				HMAC h = new HMAC(pathToDir, c.getClientName());
				String hmac = h.createHMAC(message);
				hmac = new String(Base64.encode(hmac.getBytes()));
				message += ServerConfig.ARGSEPARATOR + hmac;
				message = cryptuser.get(c.getClientName()).encodeMessage(message);

			}
			clientManager.sendFeedback(c, message);
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendToIOUnit( String message ){ ioReceiver.receiveInstruction(message); }

	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) { ioReceiver = receiver; }

	/* ---- Exit management -----------------------*/
	@Override
	public void registerExitObserver(IExitObserver e) { eObservers.add(e); }

	@Override
	public void sendExit() {
		IExitObserver ioUnit = null;
		for( IExitObserver eo : eObservers){
			if( eo instanceof IOUnit ){ ioUnit = eo; }else{ eo.exit(); }
		}
		ioUnit.exit();
	}

	@Override
	public void invokeShutdown() { timer.cancel(); this.sendExit(); }

	@Override
	public void exit() { clientManager.shutDownClient(servedClient); }


	/* --- unused Methods --------------------------*/
	@Override
	public void rejectGroupBid() {}

	@Override
	public void notifyConfirmed() {}

	@Override
	public void ok() {}

	@Override
	public void receiveFeedback(String feedback) {
		this.sendFeedback(feedback);		
	}

	public void sendTimoutReject(GroupBid groupBid) {
		ArrayList<Client> clients = groupBid.getConfirmClients();
		for( Client c : clients ){
			this.sendFeedback(c, ServerConfig.REJECTCOMMAND + ServerConfig.ARGSEPARATOR + "Confirmation out of time");
		}

	}

	@Override
	public void rejectLogin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchToOfflineMode() {
		// TODO Auto-generated method stub
		
	}


}
