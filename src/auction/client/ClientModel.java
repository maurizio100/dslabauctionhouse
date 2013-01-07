package auction.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.plaf.SliderUI;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import auction.client.interfaces.IAuctionCommandReceiverClient;
import auction.client.interfaces.IClientCommandReceiver;
import auction.commands.AuctionEndedCommand;
import auction.commands.AuctionListCommand;
import auction.commands.BidAuctionCommand;
import auction.commands.CommandRepository;
import auction.commands.ConfirmGroupBidCommand;
import auction.commands.CreateAuctionCommand;
import auction.commands.ExitCommand;
import auction.commands.GroupBidAuctionCommand;
import auction.commands.ICommand;
import auction.commands.ListCommand;
import auction.commands.LoginCommand;
import auction.commands.LoginRejectCommand;
import auction.commands.LogoutCommand;
import auction.commands.OkCommand;

import auction.commands.NotifyConfirmGroupBidCommand;
import auction.commands.OverbidCommand;
import auction.commands.RejectGroupBidCommand;
import auction.communication.interfaces.IExitObserver;
import auction.communication.interfaces.IExitSender;
import auction.communication.interfaces.IMessageReceiver;
import auction.communication.interfaces.IMessageSender;
import auction.crypt.AESCrypt;
import auction.crypt.HMAC;
import auction.crypt.ICrypt;
import auction.crypt.RSACrypt;
import auction.exceptions.NotACommandException;
import auction.interfaces.IAuctionCommandReceiverServer;
import auction.io.IOInstructionReceiver;
import auction.io.IOInstructionSender;
import auction.io.IOUnit;

public class ClientModel 
implements IMessageReceiver, IOInstructionSender, IExitSender, IAuctionCommandReceiverClient, IClientCommandReceiver, IAuctionCommandReceiverServer{

	/*--------------Communication----------------------*/
	private IMessageSender localMessenger = null;
	private IMessageReceiver networkMessenger = null;
	private IOInstructionReceiver ioReceiver = null;
	private ArrayList<IExitObserver> eObservers = null;
	private String username = null;

	private boolean loggedIn = false;
	private int udpPort = -1;
	/*-----------Cryptograhpy variables---------------*/
	private String pathToPublicKey = null;
	private String pathToPrivateKey = null;
	private ICrypt crypt = null;
	private byte[] secureNumber = new byte[32];

	/*-----------GroupBid management-----------------*/
	private boolean confirmationSent = false;

	/*-----------Command Management-------------------*/
	private String currentCommand = "";
	private String[] splittedString;
	private CommandRepository commandRepository = null;
	private ICommand[] availableCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new OverbidCommand(this),
			new AuctionEndedCommand(this),
			new OkCommand(this),
			new GroupBidAuctionCommand(this),
			new RejectGroupBidCommand(this),
			new ConfirmGroupBidCommand(this),
			new NotifyConfirmGroupBidCommand(this),
			new LoginRejectCommand(this)
	};
	private boolean offlinemode = false;


	/* ------------------- Constructors ------------------ */
	public ClientModel(IMessageSender lmc,
			IMessageReceiver nmfc, int udpPort) {

		localMessenger = lmc;
		networkMessenger = nmfc;

		localMessenger.registerMessageReceiver(this);
		eObservers = new ArrayList<IExitObserver>();

		commandRepository = new CommandRepository(availableCommands);
		this.udpPort = udpPort;
	}

	public ClientModel(IMessageSender lmc,
			IMessageReceiver nmfc, int udpPort, String pathToPublicKey, String pathToPrivateKey) {
		this(lmc, nmfc, udpPort);
		this.pathToPrivateKey = pathToPrivateKey;
		this.pathToPublicKey = pathToPublicKey;

		/* client-challenge erstellen und in Base64 umwandeln wegen leerzeichen */
		new SecureRandom().nextBytes(secureNumber);
		secureNumber = Base64.encode(secureNumber);
	}

	/* -------- Message parsing and processing ---------------*/
	@Override
	public void receiveMessage(String message) {
		parseMessage(message);
	}

	private boolean isCommand(String message) {
		if( message.length()  < 1 ) return false;
		return message.charAt(0) == '!';
	}

	private ICommand parseCommand(String command) throws NotACommandException{
		splittedString = command.split(" ");
		ICommand c = commandRepository.checkCommand(splittedString[0]);
		if ( c == null ) throw new NotACommandException(splittedString[0] + " is not a command!");

		return c;
	}

	private void parseMessage(String message) {
		if(!message.isEmpty())
		{
			try{
				ICommand c = null;
				if( this.isCommand(message) ){
					c = parseCommand(message);
					currentCommand = message;
				}
				else if(crypt != null)
				{
					/* decrypt messages */
					message = crypt.decodeMessage(message);
					if( this.isCommand(message) ){
						c = parseCommand(message);
						currentCommand = message;
					}
				}		
				if(c!= null)
				{
					c.execute();
					currentCommand = null;
				}
				else{
					if(loggedIn)
					{
						String content = "";
						splittedString = message.split(" ");
						for(int i = 0; i<splittedString.length-1; i++)
						{
							content += splittedString[i];
							if(i < splittedString.length-2 )
							{
								content += " ";
							}
						}
						if(checkHMAC(splittedString[(splittedString.length-1)], content))
						{
							sendToIOUnit(content);
						}
						else
						{
							sendToIOUnit("Error HMAC is not equal");
						}
					}
					else
					{
						sendToIOUnit(message);
					}
				}
			}catch( NotACommandException nace ){
				ioReceiver.receiveInstruction(nace.getMessage());
			}
		}
	}

	private void sendSyntaxError( String command, String correctSyntax){
		String message = "Syntaxerror in Command: " + command + "\n";
		message += "Correct usage: " + correctSyntax;

		sendToIOUnit(message);
	}

	/* --------------------- login management ----------------------*/
	@Override
	public void login() {
		if( loggedIn ){
			this.sendToIOUnit("You are still logged in!");
			return;
		}

		if(splittedString.length != 2){ 
			this.sendSyntaxError(splittedString[0], "!login <username>");
			return;
		}

		//		ioReceiver.setUser(splittedString[1]);

		/* password request for private key */
		PEMReader in;
		try {
			String pathPrivateKey = pathToPrivateKey + splittedString[1]+".pem";
			in = new PEMReader(new FileReader(pathPrivateKey), new PasswordFinder() {
				@Override
				public char[] getPassword() {
					// reads the password from standard input for decrypting the private key
					sendToIOUnit("Enter pass phrase:");
					return ioReceiver.performInput().toCharArray();
				}
			});
			KeyPair keyPair = (KeyPair) in.readObject(); 
			PrivateKey privateKey = keyPair.getPrivate();
			crypt = new RSACrypt(pathToPublicKey, privateKey);

			String secnum = new String(secureNumber);
			username = splittedString[1].toLowerCase();
			this.sendToNetwork(currentCommand + " " + udpPort + " " + secnum);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void ok() {
		splittedString = currentCommand.split(" ");
		byte[] clientchallenge = splittedString[1].getBytes();

		String c = new String(clientchallenge); 
		String s = new String(secureNumber);
		
		if(s.equals(c))
		{

			byte[] key = Base64.decode(splittedString[3].getBytes());
			byte[] iv = Base64.decode(splittedString[4].getBytes());

			Key secretkey = new SecretKeySpec(key, "AES");
			crypt = new AESCrypt(secretkey, iv);

			this.sendToNetwork(splittedString[2]);
			this.sendToIOUnit("Login Successful");
			loggedIn = true;
			
		}
		else
		{
			this.sendToIOUnit("Login Failed!");
			username = null;
		}
	}


	@Override
	public void logout() {
		if( !loggedIn ){
			this.sendToIOUnit("You are still logged out!");
			return;
		}
		if(splittedString.length != 1){
			this.sendSyntaxError(splittedString[0], "!logout");
			return;
		}
		crypt = null;
		this.sendToNetwork(currentCommand);
		ioReceiver.resetUser();
		loggedIn = false;
		username = null;
	}

	/* ------------- Auction management ------------------------------ */
	@Override
	public void createAuction() {
		if( !loggedIn ){
			this.sendToIOUnit("Auction cant be created - You are not Logged in!");
			return;
		}

		splittedString = currentCommand.split(" ", 3);
		if(splittedString.length != 3){
			this.sendSyntaxError(splittedString[0], "!create <duration> <description>");
			return;
		}

		this.sendToNetwork(currentCommand);	
	}

	@Override
	public void bidForAuction() {
		if( !loggedIn ){
			this.sendToIOUnit("Auction cant be created - You are not Logged in!");
			return;
		}
		splittedString = currentCommand.split(" ", 3);
		if( splittedString.length != 3 ){
			this.sendSyntaxError(splittedString[0], "bid <auction-id> <amount>");
			return;
		}

		this.sendToNetwork(currentCommand);
	}

	@Override
	public void list() {
		this.sendToNetwork(currentCommand);
	}


	@Override
	public void overbid() {
		splittedString = currentCommand.split(" ", 2);
		String message = "You have been overbid on '" + splittedString[1] + "'";
		this.sendToIOUnit(message);

	}

	@Override
	public void endAuction() {
		splittedString = currentCommand.split(" ", 4 );
		String winner = splittedString[1];
		String price = splittedString[2];
		String description = splittedString[3];

		String message = "The auction " + description + " has ended. " + winner + " won with " + price + "."; 

		this.sendToIOUnit(message);

	}

	/* ------------ Group Bid --------------------------*/
	@Override
	public void confirmGroupBid() {
		if( !loggedIn ){
			this.sendToIOUnit("Auction cant be created - You are not Logged in!");
			return;
		}
		splittedString = currentCommand.split(" ", 4);
		if( splittedString.length != 4 ){
			this.sendSyntaxError(splittedString[0], "confirm <auction-id> <bid> <Username>");
			return;
		}

		this.sendToNetwork(currentCommand);
		confirmationSent = true;
		while(confirmationSent){
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {

			}
		}
		
	}

	@Override
	public void rejectGroupBid() {
		confirmationSent = false;
		splittedString = currentCommand.split(" ", 3);
		this.sendToIOUnit("Error at confirmation: " + splittedString[1] );
	}

	@Override
	public void notifyConfirmed() {
		confirmationSent = false;
		this.sendToIOUnit("Groupbid confirmed.");
	}


	/*------------------- Network Messaging --------------------*/
	private void sendToNetwork(String message){
		/* if user is logged in then encryption of the message */
		if(crypt != null)
		{
			message = crypt.encodeMessage(message);
		}
		networkMessenger.receiveMessage(message);
	}

	/*------------------------IO-Unit--------------------------------_*/
	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) {
		ioReceiver = receiver;
	}

	private void sendToIOUnit( String message ){
		ioReceiver.receiveInstruction(message);
	}

	/* ---------- Exit management ------------ */
	@Override
	public void registerExitObserver(IExitObserver e) {
		eObservers.add(e);
	}

	@Override
	public void exit() {
		
		if(loggedIn){
			this.logout();
			loggedIn = false;
			username = null;
			ioReceiver.resetUser();
		}
		this.sendExit();
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
	public void invokeShutdown() {
		this.sendExit();
	}

	private boolean checkHMAC(String hash, String content) {
		
		HMAC h;
		try {
			hash = new String(Base64.decode(hash.getBytes()));
			h = new HMAC(pathToPrivateKey, username);
			return h.checkHMAC(content, hash);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void rejectLogin() {
		splittedString = currentCommand.split(" ", 2);
		crypt = null;
		username = null;
		loggedIn = false;
		
		sendToIOUnit(splittedString[1]);
		
	}

	@Override
	public void switchToOfflineMode() {
		crypt = null;
		loggedIn = false;
		offlinemode  = true;
		
	}

}
