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

import auction.global.commands.BidAuctionCommand;
import auction.global.commands.CommandRepository;
import auction.global.commands.ConfirmGroupBidCommand;
import auction.global.commands.CreateAuctionCommand;
import auction.global.commands.ExitCommand;
import auction.global.commands.GroupBidAuctionCommand;
import auction.global.commands.ListCommand;
import auction.global.commands.LoginCommand;
import auction.global.commands.LogoutCommand;
import auction.global.config.CommandConfig;
import auction.global.config.GlobalConfig;
import auction.global.config.ServerConfig;
import auction.global.crypt.AESCrypt;
import auction.global.crypt.HMAC;
import auction.global.crypt.RSACrypt;
import auction.global.exceptions.BidTooLowException;
import auction.global.exceptions.BidderNotAvailableException;
import auction.global.exceptions.BidderSameConfirmException;
import auction.global.exceptions.ProductNotAvailableException;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;
import auction.global.interfaces.ICommandReceiver;
import auction.global.interfaces.ICommandSender;
import auction.global.interfaces.ICrypt;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;
import auction.global.interfaces.ILocalMessageSender;
import auction.global.io.IOInstructionReceiver;
import auction.global.io.IOInstructionSender;
import auction.global.io.IOUnit;
import auction.server.interfaces.IAuctionActivityReceiver;
import auction.server.interfaces.IAuctionOperator;
import auction.server.interfaces.IClientOperator;
import auction.server.interfaces.IClientThread;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

public class ServerModel 
implements IExitSender, IAuctionCommandReceiver, ICommandReceiver,
ILocalMessageReceiver, IOInstructionSender, IAuctionActivityReceiver{

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
	private IClientThread servedClient = null;

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
			new GroupBidAuctionCommand(this),
			new ConfirmGroupBidCommand(this)
	};

	/* ---- Constructors ---------------------------- */
	public ServerModel(ILocalMessageSender lmc,
			ICommandSender cc, IClientOperator clientManager) {

		eObservers = new ArrayList<IExitObserver>();		
		groupBids = new HashMap<Integer,GroupBid>();

		commandRepository = new CommandRepository(availableCommands);
		queuedGroupBids = new GroupBidQueue();
		auctionManager = new AuctionManager(this,this);
		this.clientManager = clientManager;
		timer = new Timer();

		lmc.registerMessageReceiver(this);
		cc.registerCommandReceiver(this);
	}

	public ServerModel(ILocalMessageSender lmc,
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
	public void receiveLocalMessage(String message) {
		if(message.isEmpty()){
			this.sendToIOUnit(ServerConfig.SHUTDOWNNOTIFICATION);
			this.invokeShutdown();
		}else{ parseMessage( message ); }
	}

	@Override
	public void receiveCommand(String command, Client source) {
		this.parseNetworkMessage(command, source);
	}

	@Override
	public void receiveAuctionNotification(String message) {
		this.receiveAuctionNotification( message, servedClient );
	}
	
	@Override
	public void receiveAuctionNotification(String message, IClientThread client) {
		this.sendFeedback(client, message);
	}

	private void parseNetworkMessage( String message, Client source ){

		if( this.isCommand(message) ){
			this.processCommand( message, source );
		}else if(cryptuser.containsKey(source.getClientName())){
			message = cryptuser.get(source.getClientName()).decodeMessage(message);			}
		else{
			message = crypt.decodeMessage(message);
		}

		if( this.isCommand(message) ){
			this.processCommand(message, source);		
		}else{
			checkClientServerChallenge(message.getBytes(), source);
		}
	}


	private void parseMessage(String message) {
		if( this.isCommand(message) ){
			this.processCommand(message, null);
		}else{ 	this.sendToIOUnit(message); }
	}


	private boolean isCommand(String message) { return message.charAt(0) == CommandConfig.COMMANDNOTIFIER; }

	private ICommand parseCommand(String command){
		splittedString = command.split(CommandConfig.ARGSEPARATOR,2);
		String extractedCommand = splittedString[CommandConfig.POSCOMMAND];
		return commandRepository.checkCommand(extractedCommand);
	}
	
	private void processCommand( String message, IClientThread source ){
		ICommand c = null;
		currentCommand = message;
		if ( source != null ){
			servedClient = source;
			synchronized(servedClient){
				c = parseCommand(message);
				if( c != null ){ c.execute(); }
				servedClient = null;
			}
			currentCommand = null;
		}else{
			c = parseCommand(message);
			if( c != null ){ c.execute(); }	
		}
	}
	
	private void sendGroupBidNotification(GroupBid groupBid) {
		IClientThread groupBidder = groupBid.getGroupBidder();

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
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, ServerConfig.LOGINSERVERTOKENCOUNT );
			
			String clientName = splittedString[CommandConfig.POSCLIENTNAME];
			int udpPort = Integer.parseInt(splittedString[CommandConfig.POSUDPPORT]);
			String clientchallenge = splittedString[ServerConfig.POSLOGINCLIENTCHALLENGE];

			String userPublicKey = pathToDir + clientName + GlobalConfig.PUBLICKEYFILEPOSTFIX;

			/* Response via TCP */
			if( !cryptuser.containsKey(clientName) )
			{
				try {
					clientManager.loginClient(clientName, udpPort, servedClient);
					cryptuser.put(clientName, new RSACrypt(userPublicKey, privateKey));

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


					generator = KeyGenerator.getInstance("AES");
					generator.init(256); 
					key = generator.generateKey(); 


					String secretkey = new String(Base64.encode(key.getEncoded()));
					String serverchallenge = new String(secureNumberUser.get(clientName));
					String ivparam = new String(iv);
				
					String cryptmessage = CommandConfig.COMMANDNOTIFIER + CommandConfig.OK + CommandConfig.ARGSEPARATOR + clientchallenge + CommandConfig.ARGSEPARATOR 
							+ serverchallenge + CommandConfig.ARGSEPARATOR + secretkey + CommandConfig.ARGSEPARATOR + ivparam;

					this.sendFeedback(cryptmessage);
					cryptuser.put(clientName, new AESCrypt(key, iv));
					
				} catch (IOException e) {
					this.sendLoginRject(ServerConfig.LOGINERROR, clientName);
				} catch (NoSuchAlgorithmException e) {
					this.sendLoginRject(ServerConfig.LOGINERROR, clientName);
				}
			}else{
				this.sendLoginRject(ServerConfig.USERLOGGEDINERROR, clientName);
			}
		}catch(NumberFormatException nfe){
			this.sendFeedback(ServerConfig.PORTFORAMTERROR);
		} 
	}

	@Override
	public void logout() {
		clientManager.logoffClient(servedClient);
		cryptuser.remove(servedClient.getClientName());
	}

	private void checkClientServerChallenge(byte[] number, IClientThread source){

		String n = new String(number);
		String cn = new String(secureNumberUser.get(source.getClientName()));
		String client = source.getClientName();
		
		if(n.equals(cn)){
			sendToIOUnit(client + " Logged in successfully!");		
		}else{
			this.sendLoginRject(ServerConfig.LOGINERROR, client);
		}
	}
	
	/* ---------- Auction Management ------------------------- */	
	@Override
	public void createAuction() {
		try{
			
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.CREATEAUCTIONTOKENCOUNT);
			
			int time = Integer.parseInt(splittedString[CommandConfig.POSAUCTIONTIME]);
			String description = splittedString[CommandConfig.POSAUCTIONDESCRIPTION];
			String client = servedClient.getClientName();

			/*int i = 2;

			for (; i < (splittedString.length-1); i++ ){
				description += splittedString[i];
				description += " ";
			}
			description += splittedString[i];
			*/
			auctionManager.addAuction(description, client, time);

		}catch(NumberFormatException nfe){this.sendFeedback(ServerConfig.TIMEFORMATERROR);}
	}

	@Override
	public void bidForAuction() {
		try{
			
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.BIDAUCTIONTOKENCOUNT);
			
			int auctionNumber = Integer.parseInt(splittedString[CommandConfig.POSAUCTIONNUMBER]);
			double bid = Double.parseDouble(splittedString[CommandConfig.POSBID]);
			String groupBidCommand = CommandConfig.COMMANDNOTIFIER + CommandConfig.GROUPBID;
			String client = servedClient.getClientName();
			
			if( !auctionManager.isAuctionIdAvailable(auctionNumber)){ throw new ProductNotAvailableException(); }

			/* --- GroupBid part ---------------- */
			if( splittedString[CommandConfig.POSCOMMAND].equals( groupBidCommand )){
				GroupBid gb = new GroupBid(auctionNumber,bid, servedClient, this);
				this.addGroupBid(gb);
				
			}else{ auctionManager.bidForAuction(auctionNumber, client, bid); }

		}catch( NumberFormatException nfe ){
			this.sendFeedback(ServerConfig.BIDNUMBERFORMATERROR);
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback(ServerConfig.AUCTIONNOTAVAILABLEERROR);
		}
	}

	private void addGroupBid(GroupBid gb) {
		if( groupBids.size() < cryptuser.size() ){			

			if( !groupBids.containsKey( gb.getAuctionNumber() ) ){
				groupBids.put( gb.getAuctionNumber(), gb);
				timer.schedule(gb, ServerConfig.GROUPBIDDELAY, ServerConfig.GROUPBIDPERIOD);
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
		String message = auctionManager.listAuction();			
		this.sendFeedback(message);
	}

	@Override
	public void overbid(String auctionDescription, String bidderName) {
		
		/*
		String notification = splittedString[0] + CommandConfig.ARGSEPARATOR;
		int i = 1;
		for( ; i < splittedString.length-1; i++ ){ notification += splittedString[i]; }

		String lastBidder = splittedString[i];
		clientManager.sendNotification(notification, lastBidder);
		*/
	}

	@Override
	public void endAuction(Auction auction) {
		/*
		String notification = splittedString[0] + ServerConfig.ARGSEPARATOR + splittedString[1] + ServerConfig.ARGSEPARATOR + splittedString[2] + ServerConfig.ARGSEPARATOR;
		int i = 3;
		for( ; i < splittedString.length-1; i++ ){ notification += splittedString[i]; }

		String receiver = splittedString[i];
		clientManager.sendNotification(notification, receiver);
		*/
	}
	/* --------- GroupBid management -------------------------------- */
	@Override
	public void confirmGroupBid() {
		String rejectCommand = CommandConfig.COMMANDNOTIFIER + CommandConfig.REJECTED;
		try{
			
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.CONFIRMTOKENCOUNT);
			
			int auctionNumber = Integer.parseInt(splittedString[CommandConfig.POSCONFIRMAUCTIONNUMBER]);
			double bid = Double.parseDouble(splittedString[CommandConfig.POSCONFIRMBID]);
			String groupBidder = splittedString[CommandConfig.POSCONFIRMGROUPBIDDER].toLowerCase();
			
			
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
			this.sendFeedback(rejectCommand + " Couldn't confirm groupAuction: auctionNumber and bid must be numeric!" );
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback(rejectCommand + " The AuctionId you want to confirm to is not available!");
		}catch( BidderNotAvailableException bnae){
			this.sendFeedback(rejectCommand + " The Client you want to confirm has not bid to an auction with the given ID!");
		}catch( BidTooLowException btle ){
			this.sendFeedback(rejectCommand + " Your bid is not equal to that of the group\'s bidder!");
		} catch (BidderSameConfirmException e) {
			this.sendFeedback(rejectCommand + " You have set the GroupBid!");
		}

	}

	private void notifyClients(GroupBid gb) {
		ArrayList<IClientThread> confirmers = gb.getConfirmClients();
		for( IClientThread c : confirmers ){
			this.sendFeedback(c, CommandConfig.COMMANDNOTIFIER + CommandConfig.CONFIRMED);
		}
	}

	private void confirmBid(GroupBid gb) {
		int auctionNumber = gb.getAuctionNumber();
		double bid = gb.getBid();
		IClientThread groupBidder = gb.getGroupBidder();
		
		try{
			auctionManager.bidForAuction(auctionNumber, groupBidder.getClientName(), bid);
		}catch( ProductNotAvailableException pnae ){
			this.sendFeedback(groupBidder, "The product you want to bid is not available anymore!");
		}
	}

	public void sendTimoutReject(GroupBid groupBid) {
		String rejectCommand = CommandConfig.COMMANDNOTIFIER + CommandConfig.REJECTED;
		ArrayList<IClientThread> clients = groupBid.getConfirmClients();
		for( IClientThread c : clients ){
			this.sendFeedback(c, rejectCommand + CommandConfig.ARGSEPARATOR + "Confirmation out of time");
		}
	}
	/* ---------- Communication --------------- */
	private void sendLoginRject(String message, String clientName){
		String loginReject = CommandConfig.COMMANDNOTIFIER + CommandConfig.LOGINREJECT;
		message = loginReject + CommandConfig.ARGSEPARATOR + message;
		try {
			String userPublicKey = pathToDir + clientName + GlobalConfig.PUBLICKEYFILEPOSTFIX;
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
				message += CommandConfig.ARGSEPARATOR + hmac;
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

	private void sendToIOUnit( String message ){ ioReceiver.processInstruction(message); }

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

	private void invokeShutdown() { timer.cancel();  this.sendExit(); }

	@Override
	public void exit() { if( servedClient.isLoggedIn() ) { this.logout(); } clientManager.shutDownClient(servedClient); }

}
