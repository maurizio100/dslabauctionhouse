package auction.server;

import java.util.ArrayList;
import java.util.HashMap;

import auction.commands.AuctionCommandReceiverServer;
import auction.commands.AuctionEndedCommand;
import auction.commands.BidAuctionCommand;
import auction.commands.ClientCommandReceiver;
import auction.commands.Command;
import auction.commands.CommandRepository;
import auction.commands.ConfirmGroupBidCommand;
import auction.commands.CreateAuctionCommand;
import auction.commands.ExitCommand;
import auction.commands.GroupBidAuctionCommand;
import auction.commands.ListCommand;
import auction.commands.LoginCommand;
import auction.commands.LogoutCommand;
import auction.commands.OverbidCommand;
import auction.communication.CommandReceiver;
import auction.communication.CommandSender;
import auction.communication.ExitObserver;
import auction.communication.ExitSender;
import auction.communication.MessageReceiver;
import auction.communication.MessageSender;
import auction.exceptions.BidTooLowException;
import auction.exceptions.BidderNotAvailableException;
import auction.exceptions.ProductNotAvailableException;
import auction.io.IOInstructionReceiver;
import auction.io.IOInstructionSender;
import auction.io.IOUnit;


public class ServerModel 
implements ExitSender, AuctionCommandReceiverServer, ClientCommandReceiver, CommandReceiver,MessageReceiver, IOInstructionSender{

	private ArrayList<ExitObserver> eObservers = null;
	private HashMap<Integer, HashMap<String, GroupBid>> groupBids = null;
	private GroupBidQueue queuedGroupBids = null; 
	
	private CommandRepository commandRepository = null;
	private String currentCommand = "";
	private Client servedClient = null;
	private String[] splittedString;
	private IOInstructionReceiver ioReceiver = null;

	private AuctionOperator auctionManager = null; 
	private ClientOperator clientManager = null;

	private Command[] availableCommands = {
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

	public ServerModel(MessageSender lmc,
			CommandSender cc, ClientOperator clientManager) {

		eObservers = new ArrayList<ExitObserver>();		
		groupBids = new HashMap<Integer,HashMap<String, GroupBid>>();
		
		commandRepository = new CommandRepository(availableCommands);
		this.clientManager = clientManager;
		queuedGroupBids = new GroupBidQueue();
		
		auctionManager = new AuctionManager(this,this);
		lmc.registerMessageReceiver(this);
		cc.registerCommandReceiver(this);
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
	public void receiveCommand(String command, Client source) {
		servedClient = source;
		this.parseMessage(command);
		servedClient = null;
	}


	private void parseMessage(String message) {
		if( this.isCommand(message) ){
			Command c = parseCommand(message);
			currentCommand = message;
			c.execute();
			currentCommand = null;
		}
	}

	private Command parseCommand(String command){
		splittedString = command.split(" ");
		Command c = commandRepository.checkCommand(splittedString[0]);
		return c;
	}


	private boolean isCommand(String message) {
		return message.charAt(0) == '!';
	}

	private void sendGroupBidNotification(GroupBid groupBid) {
		clientManager.sendGroupBidNotification(groupBid);	
	}
	
	@Override
	public void registerExitObserver(ExitObserver e) {
		eObservers.add(e);
	}

	@Override
	public void sendExit() {
		ExitObserver ioUnit = null;
		for( ExitObserver eo : eObservers){
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
			//TODO R�ckmeldung �ber TCP
			servedClient.receiveFeedback("!ok");
		}catch(NumberFormatException nfe){
			servedClient.receiveFeedback("Couldn't login: The udpPort must be numeric and digit between 1024 and 65535!");
		}
	}

	@Override
	public void logout() {
		clientManager.logoffClient(servedClient);
		servedClient.receiveFeedback("You are logged out now.");
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
			servedClient.receiveFeedback("Couldn't create auction: The time must be numeric.");
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
					servedClient.receiveFeedback("There are already too much groupBids! The Bid will be set when it is possible!");
					queuedGroupBids.enqueue(gb);
				}
				
			}else{
				auctionManager.bidForAuction(auctionNumber, servedClient, bid);
			}
			
		}catch( NumberFormatException nfe ){
			clientManager.sendFeedback(servedClient, "Couldn't bid for auction: auctionNumber and bid-number must be numeric.");
		}catch( ProductNotAvailableException pnae){
			clientManager.sendFeedback(servedClient, "The Auction you want to bid is not available.");
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
	public void ok() {}

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
			clientManager.sendFeedback(servedClient, "!reject Couldn't confirm groupAuction: auctionNumber and bid must be numeric!" );
		}catch( ProductNotAvailableException pnae){
			clientManager.sendFeedback(servedClient, "!reject The AuctionId you want to confirm to is not available!");
		}catch( BidderNotAvailableException bnae){
			clientManager.sendFeedback(servedClient, "!reject The Client you want to confirm has not bid to an auction with the given ID!");
		}catch( BidTooLowException btle ){
			clientManager.sendFeedback(servedClient, "!reject Your bid is not equal to that of the group\'s bidder!");
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
			clientManager.sendFeedback(groupBidder, "The product you want to bid is not available anymore!");
			
		}
	}

	@Override
	public void rejectGroupBid() {}

	@Override
	public void notifyConfirmed() {}

}
