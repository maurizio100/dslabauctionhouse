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

import auction.commands.AuctionCommandReceiverClient;
import auction.commands.AuctionCommandReceiverServer;
import auction.commands.AuctionEndedCommand;
import auction.commands.BidAuctionCommand;
import auction.commands.ClientCommandReceiver;
import auction.commands.Command;
import auction.commands.CommandRepository;
import auction.commands.CreateAuctionCommand;
import auction.commands.ExitCommand;
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
import auction.crypt.AESCrypt;
import auction.crypt.Crypt;
import auction.crypt.RSACrypt;
import auction.io.IOInstructionReceiver;
import auction.io.IOInstructionSender;
import auction.io.IOUnit;


public class ServerModel 
implements ExitSender, AuctionCommandReceiverClient, AuctionCommandReceiverServer, ClientCommandReceiver, CommandReceiver,MessageReceiver, IOInstructionSender{

	private ArrayList<ExitObserver> eObservers = null;
	private CommandRepository commandRepository = null;
	private String currentCommand = "";
	private Client servedClient = null;
	private String[] splittedString;
	private IOInstructionReceiver ioReceiver = null;

	private AuctionOperator auctionManager = null; 
	private ClientOperator clientManager = null;
	private HashMap<String, Crypt> cryptuser = null;
	private Crypt crypt = null;
	private String pathToPublicKey = null;
	private PrivateKey privateKey = null;
	private HashMap<String, byte[]> secureNumberUser = new HashMap<String, byte[]>();
	private String pathToDir = null;

	private Command[] availableCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new AuctionEndedCommand(this),
			new OverbidCommand(this)
	};

	public ServerModel(MessageSender lmc,
			CommandSender cc, ClientOperator clientManager) {

		eObservers = new ArrayList<ExitObserver>();		
		commandRepository = new CommandRepository(availableCommands);
		this.clientManager = clientManager;

		auctionManager = new AuctionManager(this,this);
		lmc.registerMessageReceiver(this);
		cc.registerCommandReceiver(this);
	}

	public ServerModel(MessageSender lmc,
			CommandSender cc, ClientOperator clientManager, String pathToPublicKey,PrivateKey privateKey, String pathToDir) {

		this(lmc, cc, clientManager);
		this.privateKey = privateKey;
		this.pathToPublicKey = pathToPublicKey;
		this.pathToDir = pathToDir;
		//server-challenge erstellen und in Base64 umwandeln wegen leerzeichen

		this.cryptuser = new HashMap<String, Crypt>();
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
		Command c = null;
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

	private Command parseCommand(String command){
		splittedString = command.split(" ");
		Command c = commandRepository.checkCommand(splittedString[0]);
		return c;
	}


	private boolean isCommand(String message) {
		return message.charAt(0) == '!';
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

			String userPublicKey = pathToDir +clientName+".pub.pem";

			//Rückmeldung über TCP
			if(!cryptuser.containsKey(clientName))
			{
				try {
					cryptuser.put(clientName, new RSACrypt(userPublicKey, privateKey));
				} catch (IOException e) {
					e.printStackTrace();
				}



				//Für jeden Client neue server-challenge
				byte[] number = new byte[32];
				new SecureRandom().nextBytes(number);
				number = Base64.encode(number);			
				secureNumberUser.put(clientName, number);

				//Für Iv-Param
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

				
				servedClient.receiveFeedback(cryptmessage);
				
				cryptuser.put(clientName, new AESCrypt(key, iv));
			}
			else
			{
				servedClient.receiveFeedback("User alredy logged in!");
			}

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

			auctionManager.addAuction(description, servedClient, time);
		}catch(NumberFormatException nfe){
			servedClient.receiveFeedback("Couldn't create auction: The time must be numeric.");
		}
	}

	@Override
	public void bidForAuction() {

		try{
			int auctionNumber = Integer.parseInt(splittedString[1]);
			double bid = Double.parseDouble(splittedString[2]);

			auctionManager.bidForAuction(auctionNumber, servedClient, bid);
		}catch( NumberFormatException nfe ){
			servedClient.receiveFeedback("Couldn't bid for auction: auctionNumber and bid-number must be numeric.");
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
}
