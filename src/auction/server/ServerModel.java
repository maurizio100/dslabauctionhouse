package auction.server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
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
import auction.crypt.RSACrypt;
import auction.exceptions.BidTooLowException;
import auction.exceptions.BidderNotAvailableException;
import auction.exceptions.ProductNotAvailableException;
import auction.interfaces.IAuctionCommandReceiverClient;
import auction.interfaces.IAuctionCommandReceiverServer;
import auction.interfaces.IAuctionOperator;
import auction.interfaces.IClientCommandReceiver;
import auction.interfaces.IClientOperator;
import auction.interfaces.ICommand;
import auction.interfaces.ICommandReceiver;
import auction.interfaces.ICommandSender;
import auction.interfaces.ICrypt;
import auction.interfaces.IExitObserver;
import auction.interfaces.IExitSender;
import auction.interfaces.IOInstructionReceiver;
import auction.interfaces.IOInstructionSender;
import auction.interfaces.IMessageReceiver;
import auction.interfaces.IMessageSender;
import auction.io.IOUnit;


public class ServerModel 
implements IExitSender, IAuctionCommandReceiverServer, IClientCommandReceiver, ICommandReceiver,IMessageReceiver, IOInstructionSender{

	private ArrayList<IExitObserver> eObservers = null;
	private HashMap<Integer, HashMap<String, GroupBid>> groupBids = null;
	private GroupBidQueue queuedGroupBids = null; 
	
	private CommandRepository commandRepository = null;
	private String currentCommand = "";
	private Client servedClient = null;
	private String[] splittedString;
	private IOInstructionReceiver ioReceiver = null;

	private IAuctionOperator auctionManager = null; 
	private IClientOperator clientManager = null;
	private HashMap<String, ICrypt> cryptuser = null;
	private ICrypt crypt = null;
	private String pathToPublicKey = null;
	private PrivateKey privateKey = null;
	private HashMap<String, byte[]> secureNumberUser = new HashMap<String, byte[]>();
	private String pathToDir = null;

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

	public ServerModel(IMessageSender lmc,
			ICommandSender cc, IClientOperator clientManager) {

		eObservers = new ArrayList<IExitObserver>();		
		groupBids = new HashMap<Integer,HashMap<String, GroupBid>>();
		
		commandRepository = new CommandRepository(availableCommands);
		this.clientManager = clientManager;
		queuedGroupBids = new GroupBidQueue();
		
		auctionManager = new AuctionManager(this,this);
		lmc.registerMessageReceiver(this);
		cc.registerCommandReceiver(this);
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

	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) {
		ioReceiver = receiver;
	}

	@Override
	public void receiveMessage(String message) {
		if(message.length() == 0){
			this.sendToIOUnit("Shutting down Server!");
			sendExit();
		}
		this.sendToIOUnit(message);
	}

	private void sendToIOUnit( String message ){
		ioReceiver.receiveInstruction(message);
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
			message = crypt.decodeMessage(message);
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
		splittedString = command.split(" ");
		ICommand c = commandRepository.checkCommand(splittedString[0]);
		return c;
	}


	private boolean isCommand(String message) {
		return message.charAt(0) == '!';
	}

	private void sendGroupBidNotification(GroupBid groupBid) {
		clientManager.sendGroupBidNotification(groupBid);	
	}
	
	@Override
	public void registerExitObserver(IExitObserver e) {
		eObservers.add(e);
	}

	@Override
	public void sendExit() {
		IExitObserver ioUnit = null;
		for( IExitObserver eo : eObservers){
			if( eo instanceof IOUnit ){ ioUnit = eo; }else{ eo.exit(); }
		}
		ioUnit.exit();
	}

	@Override
	public void login() {
		try{

			String clientName = splittedString[1];
			int udpPort = Integer.parseInt(splittedString[2]);
			clientManager.loginClient(clientName, udpPort, servedClient);

			String userPublicKey = pathToDir +clientName+".pub.pem";

			//R�ckmeldung �ber TCP
			if(!cryptuser.containsKey(clientName))
			{
				try {
					cryptuser.put(clientName, new RSACrypt(userPublicKey, privateKey));
				} catch (IOException e) {
					e.printStackTrace();
				}



				//F�r jeden Client neue server-challenge
				byte[] number = new byte[32];
				new SecureRandom().nextBytes(number);
				number = Base64.encode(number);			
				secureNumberUser.put(clientName, number);

				//F�r Iv-Param
				byte[] iv = new byte[16];
				new SecureRandom().nextBytes(iv);
				iv = Base64.encode(iv);


				KeyGenerator generator;
				SecretKey key = null;
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
				String clientchallenge = splittedString[3];

				sendToIOUnit(secretkey + " " + ivparam);
				
				String cryptmessage = "!ok"+" "+ clientchallenge + " " + serverchallenge + " " + secretkey + " " + ivparam;

				cryptmessage = cryptuser.get(clientName).encodeMessage(cryptmessage);

				
				this.sendFeedback(cryptmessage);
				
				cryptuser.put(clientName, new AESCrypt(key, iv));
			}
			else
			{
				this.sendFeedback("User alredy logged in!");
			}

		}catch(NumberFormatException nfe){
			this.sendFeedback("Couldn't login: The udpPort must be numeric and digit between 1024 and 65535!");
		} 
	}

	@Override
	public void logout() {
		clientManager.logoffClient(servedClient);
		this.sendFeedback("You are logged out now.");
	}

	@Override
	public void createAuction() {

		try{
			int time = Integer.parseInt(splittedString[1]);
			String description = "";

			int i = 2;

			for (; i < (splittedString.length-1); i++ ){
				description += splittedString[i];
				description += " ";
			}
			description += splittedString[i];

			int auctionId = auctionManager.addAuction(description, servedClient, time);
			groupBids.put(auctionId, new HashMap<String, GroupBid>() );
			
		}catch(NumberFormatException nfe){
			this.sendFeedback("Couldn't create auction: The time must be numeric.");
		}
	}

	@Override
	public void bidForAuction() {

		try{
			
			int auctionNumber = Integer.parseInt(splittedString[1]);
			double bid = Double.parseDouble(splittedString[2]);
						
			if( auctionManager.isAuctionIdAvailable(auctionNumber)){
				throw new ProductNotAvailableException();
			}
			
			if( currentCommand.equals("groupBid")){
				GroupBid gb = new GroupBid(auctionNumber,bid, servedClient);
				if( groupBids.size() < auctionManager.getAuctionAmount() ){			
					//TODO is a group on the same auction allowed???
					HashMap<String, GroupBid> clientsBids = groupBids.get(auctionNumber);
					clientsBids.put(servedClient.getClientName(), new GroupBid(auctionNumber, bid, servedClient));
					
					this.sendGroupBidNotification(gb);
				}else{
					this.sendFeedback("There are already too much groupBids! The Bid will be set when it is possible!");
					queuedGroupBids.enqueue(gb);
				}
				
			}else{
				auctionManager.bidForAuction(auctionNumber, servedClient, bid);
			}
			
		}catch( NumberFormatException nfe ){
			this.sendFeedback("Couldn't bid for auction: auctionNumber and bid-number must be numeric.");
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback("The Auction you want to bid is not available.");
		}
	}


	@Override
	public void list() {
		auctionManager.listAuction(servedClient);
	}

	@Override
	public void exit() {
		clientManager.shutDownClient(servedClient);
	}

	@Override
	public void overbid() {
		String notification = splittedString[0] + " ";
		int i = 1;
		for( ; i < splittedString.length-1; i++ ){
			notification += splittedString[i];
		}

		String lastBidder = splittedString[i];

		clientManager.sendNotification(notification, lastBidder);
	}

	@Override
	public void endAuction() {
		String notification = splittedString[0] + " " + splittedString[1] + " " + splittedString[2] + " ";
		int i = 3;
		for( ; i < splittedString.length-1; i++ ){
			notification += splittedString[i];
		}

		String receiver = splittedString[i];
		clientManager.sendNotification(notification, receiver);
	}

	@Override
	public void invokeShutdown() {
		this.sendExit();
	}

	@Override
	public void ok() {
		// TODO Auto-generated method stub

	}

	private void checkClientServerChallenge(byte[] number)
	{
		sendToIOUnit("JA");
		//TODO
		if(!number.equals(secureNumberUser.get(servedClient.getClientName())))
		{
			//TODO send error message and reset Client
		}
		else
		{
			sendToIOUnit("login successful!");
		}
	}

	@Override
	public void confirmGroupBid() {
		try{
			int auctionNumber = Integer.parseInt(splittedString[1]);
			double bid = Double.parseDouble(splittedString[2]);
			String groupBidder = splittedString[3];
		
			if( !groupBids.containsKey(auctionNumber) ){
				throw new ProductNotAvailableException();
			}
			
			HashMap<String, GroupBid> memberBids = groupBids.get(auctionNumber);
			
			if( !memberBids.containsKey(groupBidder) ){
				throw new BidderNotAvailableException();
			}
			
			GroupBid gb = memberBids.get(groupBidder);
			
			if( !gb.isEqual( bid ) ){
				throw new BidTooLowException();
			}
			
			if( gb.addConfirmClient(servedClient) == 2){
				confirmBid(gb);
				notifyClients(gb);
				memberBids.remove(groupBidder);
				
				if( !queuedGroupBids.isEmpty() ){
					GroupBid lastQueuedBid = queuedGroupBids.dequeue();
					int queuedBidAuctionNumber = lastQueuedBid.getAuctionNumber();
					String queuedGroupBidder = lastQueuedBid.getGroupBidder().getClientName();
					
					groupBids.get(queuedBidAuctionNumber).put(queuedGroupBidder, lastQueuedBid);
					this.sendGroupBidNotification(lastQueuedBid);
				}
				
			}
			
		}catch( NumberFormatException nfe ){
			this.sendFeedback("!reject Couldn't confirm groupAuction: auctionNumber and bid must be numeric!" );
		}catch( ProductNotAvailableException pnae){
			this.sendFeedback("!reject The AuctionId you want to confirm to is not available!");
		}catch( BidderNotAvailableException bnae){
			this.sendFeedback("!reject The Client you want to confirm has not bid to an auction with the given ID!");
		}catch( BidTooLowException btle ){
			this.sendFeedback("!reject Your bid is not equal to that of the group\'s bidder!");
		}
			
	}

	private void notifyClients(GroupBid gb) {
		ArrayList<Client> confirmers = gb.getConfirmClients();
		clientManager.performConfirmNotification(confirmers);
	}

	private void confirmBid(GroupBid gb) {
		int auctionNumber = gb.getAuctionNumber();
		double bid = gb.getBid();
		Client groupBidder = gb.getGroupBidder();
		try{
			auctionManager.bidForAuction(auctionNumber, groupBidder, bid);
		}catch( ProductNotAvailableException pnae ){
			this.sendFeedback(groupBidder, "The product you want to bid is not available anymore!");
		}
	}

	private void sendFeedback( String message ){
		this.sendFeedback(servedClient,message);
	}
	
	private void sendFeedback(Client c, String message ){
				
		clientManager.sendFeedback(c, message);
		
	}
	
	@Override
	public void rejectGroupBid() {}

	@Override
	public void notifyConfirmed() {}

}
