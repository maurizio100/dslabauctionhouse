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

import auction.client.commands.AuctionEndedCommand;
import auction.client.commands.LoginRejectCommand;
import auction.client.commands.NotifyConfirmGroupBidCommand;
import auction.client.commands.OkCommand;
import auction.client.commands.OverbidCommand;
import auction.client.commands.RejectGroupBidCommand;
import auction.client.interfaces.ICommandReceiverClient;
import auction.client.interfaces.INetworkControl;
import auction.client.interfaces.INetworkMessageReceiver;

import auction.global.commands.BidAuctionCommand;
import auction.global.commands.CommandRepository;
import auction.global.commands.ConfirmGroupBidCommand;
import auction.global.commands.CreateAuctionCommand;
import auction.global.commands.ExitCommand;
import auction.global.commands.GroupBidAuctionCommand;
import auction.global.commands.ListCommand;
import auction.global.commands.LoginCommand;
import auction.global.commands.LogoutCommand;
import auction.global.config.ClientConfig;
import auction.global.config.CommandConfig;
import auction.global.config.GlobalConfig;
import auction.global.crypt.AESCrypt;
import auction.global.crypt.HMAC;
import auction.global.crypt.RSACrypt;
import auction.global.exceptions.NotACommandException;
import auction.global.interfaces.IAuctionCommandReceiver;
import auction.global.interfaces.ICommand;
import auction.global.interfaces.ICrypt;
import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;
import auction.global.interfaces.ILocalMessageSender;
import auction.global.io.IOInstructionReceiver;
import auction.global.io.IOInstructionSender;
import auction.global.io.IOUnit;

public class ClientModel 
implements ILocalMessageReceiver, INetworkMessageReceiver, IOInstructionSender, IExitSender, ICommandReceiverClient{

	/*--------------Communication----------------------*/
	private ILocalMessageSender localMessenger = null;
	private INetworkControl nwcontrol = null;
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
	
	private CommandRepository clientCommandRepository = null;
	private ICommand[] clientCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new GroupBidAuctionCommand(this),
			new ConfirmGroupBidCommand(this)
	};
	
	private CommandRepository internalCommandRepository = null;
	private ICommand[] internalCommands = {
			new OverbidCommand(this),
			new AuctionEndedCommand(this),
			new OkCommand(this),
			new RejectGroupBidCommand(this),
			new NotifyConfirmGroupBidCommand(this),
			new LoginRejectCommand(this)			
	};

	private boolean offlinemode = false;


	/* ------------------- Constructors ------------------ */
	public ClientModel(ILocalMessageSender lmc, int udpPort, INetworkControl nwcontrol) {

		localMessenger = lmc;
		localMessenger.registerMessageReceiver(this);

		this.nwcontrol = nwcontrol;
		this.nwcontrol.registerNetworkMessageReceiver(this);

		eObservers = new ArrayList<IExitObserver>();

		clientCommandRepository = new CommandRepository(clientCommands);
		internalCommandRepository = new CommandRepository(internalCommands);
		
		this.udpPort = udpPort;
	}

	public ClientModel(ILocalMessageSender lmc, int udpPort, INetworkControl nwcontrol, String pathToPublicKey, String pathToPrivateKey) {
		this(lmc, udpPort, nwcontrol);
		this.pathToPrivateKey = pathToPrivateKey;
		this.pathToPublicKey = pathToPublicKey;

		/* client-challenge erstellen und in Base64 umwandeln wegen leerzeichen */
		new SecureRandom().nextBytes(secureNumber);
		secureNumber = Base64.encode(secureNumber);
	}

	/* -------- Message parsing and processing ---------------*/
	@Override
	public void receiveNetworkMessage(String message) {
		if( !message.isEmpty() ){
			parseNetworkMessage(message);
		}
	}

	@Override
	public void receiveNetworkInformation(String info) {
		if( !info.isEmpty() ){
			sendToIOUnit(info);
		}
	}

	@Override
	public void receiveNetworkStatusMessage(String message) {
		if( !message.isEmpty() ){
			sendToIOUnit(message);
		}
	}

	@Override
	public void receiveLocalMessage(String message) {
		if( !message.isEmpty() ){
			parseLocalMessage(message);
		}
	}

	private boolean isCommand(String message) {
		if( message.length()  < 1 ) return false;
		return message.charAt(0) == CommandConfig.COMMANDNOTIFIER;
	}

	private ICommand parseCommand(String command, CommandRepository repository) throws NotACommandException{
		splittedString = command.split(CommandConfig.ARGSEPARATOR);
		String commandString = splittedString[CommandConfig.POSCOMMAND];

		ICommand c = repository.checkCommand( commandString );
		if ( c == null ) 
			throw new NotACommandException(GlobalConfig.INFOSTRING + " " + commandString + " is not a command!");

		return c;
	}

	private void parseNetworkMessage( String message ){
		try{
			ICommand c = null;
			if(crypt != null){
				/* decrypt messages */
				message = crypt.decodeMessage(message);
				if( this.isCommand(message) ){
					c = parseCommand(message, internalCommandRepository);
					currentCommand = message;
					c.execute();
				}else{
					if(loggedIn){
						String content = "";
						splittedString = message.split(CommandConfig.ARGSEPARATOR);
						for(int i = 0; i<splittedString.length-1; i++){
							content += splittedString[i];
							if(i < splittedString.length-2 )
							{
								content += CommandConfig.ARGSEPARATOR;
							}
						}

						if(checkHMAC(splittedString[(splittedString.length-1)], content)){
							sendToIOUnit(content);
						}else{
							sendToIOUnit(ClientConfig.HMACNOTEQUAL);
						}

					}else{
						sendToIOUnit(message);
					}
				}					
			}else{ sendToIOUnit(message); }
		}catch( NotACommandException nace ){ sendToIOUnit(nace.getMessage()); }
	}

	private void parseLocalMessage(String message) {
		try{
			ICommand c = null;
			if( this.isCommand(message) ){
				c = parseCommand(message, clientCommandRepository);
				currentCommand = message;
				c.execute();
				currentCommand = null;
			}

		}catch( NotACommandException nace ){ sendToIOUnit(nace.getMessage()); }
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
			this.sendToIOUnit(ClientConfig.STILLLOGGEDIN);
			return;
		}

		if(splittedString.length != 2){ 
			this.sendSyntaxError(splittedString[CommandConfig.POSCOMMAND], CommandConfig.COMMANDNOTIFIER + CommandConfig.LOGIN + " <username>");
			return;
		}

		/* password request for private key */
		PEMReader in;
		try {
			String clientName = splittedString[CommandConfig.POSCLIENTNAME];
			String pathPrivateKey = pathToPrivateKey + clientName + GlobalConfig.PRIVATEKEYPOSTFIX;
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
			username = clientName.toLowerCase();
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
		splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR);
		byte[] clientchallenge = splittedString[CommandConfig.POSCLIENTCHALLENGE].getBytes();

		String c = new String(clientchallenge); 
		String s = new String(secureNumber);

		if(s.equals(c))
		{

			byte[] key = Base64.decode(splittedString[CommandConfig.POSSECRETKEY].getBytes());
			byte[] iv = Base64.decode(splittedString[CommandConfig.POSIVPARAMETER].getBytes());

			Key secretkey = new SecretKeySpec(key, "AES");
			crypt = new AESCrypt(secretkey, iv);

			this.sendToNetwork(splittedString[CommandConfig.POSSERVERCHALLENGE]);
			this.sendToIOUnit(ClientConfig.LOGINSUCCESSFUL);
			loggedIn = true;

		}
		else{
			this.sendToIOUnit(ClientConfig.LOGINFAILED);
			username = null;
		}
	}

	@Override
	public void logout() {
		if( !loggedIn ){
			this.sendToIOUnit(ClientConfig.STILLLOGGEDOUT);
		}else if(splittedString.length != 1){
			this.sendSyntaxError(splittedString[CommandConfig.POSCOMMAND], CommandConfig.COMMANDNOTIFIER + CommandConfig.LOGOUT);
		}else {
			this.sendToNetwork(currentCommand);
			this.resetLogin();
		}
	}

	private void resetLogin(){
		crypt = null;
		loggedIn = false;
		username = null;		
		this.sendToIOUnit( ClientConfig.LOGOUTSUCCESSFUL );
	}

	@Override
	public void rejectLogin() {
		//TODO HMAC check???
		String rejectLoginMessage = splittedString[CommandConfig.POSLOGINREJECTMESSAGE];

		splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.LOGINREJECTTOKENCOUNT);
		sendToIOUnit( rejectLoginMessage );
		this.resetLogin();
	}

	@Override
	public void receiveLogoutSignal() {
		this.resetLogin();
	}
	/* ------------- Auction management ------------------------------ */
	@Override
	public void createAuction() {
		if( !loggedIn ){
			this.sendToIOUnit(ClientConfig.CREATENOTLOGGEDIN);
		}else{
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.CREATEAUCTIONTOKENCOUNT);
			if(splittedString.length != CommandConfig.CREATEAUCTIONTOKENCOUNT){
				this.sendSyntaxError(splittedString[CommandConfig.COMMANDNOTIFIER], CommandConfig.COMMANDNOTIFIER + CommandConfig.CREATEAUCTION + " <duration> <description>");
			}else{ this.sendToNetwork(currentCommand);	}
		}
	}

	@Override
	public void bidForAuction() {
		if( !loggedIn ){
			this.sendToIOUnit(ClientConfig.BIDNOTLOGGEDIN);
		}else{
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.BIDAUCTIONTOKENCOUNT);
			if( splittedString.length != CommandConfig.BIDAUCTIONTOKENCOUNT ){
				this.sendSyntaxError(splittedString[CommandConfig.POSCOMMAND], CommandConfig.COMMANDNOTIFIER + CommandConfig.BID + " <auction-id> <amount>");
			}else{ this.sendToNetwork(currentCommand);}
		}
	}

	@Override
	public void list() {
		this.sendToNetwork(currentCommand);
	}

	@Override
	public void overbid() {
		splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.OVERBIDTOKENCOUNT);
		String message = "You have been overbid on '" + splittedString[CommandConfig.POSOVERBIDAUCTION] + "'";
		this.sendToIOUnit(message);

	}

	@Override
	public void endAuction() {
		splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.AUCTIONENDEDTOKENCOUNT );
		String winner = splittedString[CommandConfig.POSAUCTIONWINNER];
		String price = splittedString[CommandConfig.POSAUCTIONPRICE];
		String description = splittedString[CommandConfig.POSAUCTIONENDDESCRIPTION];

		String message = "The auction " + description + " has ended. " + winner + " won with " + price + "."; 

		this.sendToIOUnit(message);

	}

	/* ------------ Group Bid --------------------------*/
	@Override
	public void confirmGroupBid() {
		if( !loggedIn ){
			this.sendToIOUnit(ClientConfig.CONFIRMLOGGEDOUT);
		}else{
			splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR, CommandConfig.CONFIRMTOKENCOUNT);
			if( splittedString.length != CommandConfig.CONFIRMTOKENCOUNT ){
				this.sendSyntaxError(splittedString[CommandConfig.POSCOMMAND], CommandConfig.COMMANDNOTIFIER + CommandConfig.CONFIRM + " <auction-id> <bid> <Username>");
			}else{
				this.sendToNetwork(currentCommand);
				confirmationSent = true;
				while(confirmationSent){
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {}
				}
			}
		}
	}

	@Override
	public void rejectGroupBid() {
		confirmationSent = false;

		splittedString = currentCommand.split(CommandConfig.ARGSEPARATOR);
		String HMAC = splittedString[splittedString.length-1];
		String rejectMessage = ""; 
		
		int i = 1;
		for( ; i < splittedString.length -2; i++ ){
			rejectMessage += splittedString[i] + ' ';
		}
		rejectMessage += splittedString[i];
		String content =CommandConfig.COMMANDNOTIFIER + CommandConfig.REJECTED + " " + rejectMessage;
		if( checkHMAC(HMAC, content) ){
			this.sendToIOUnit("Error at confirmation: " + rejectMessage );
		}else{
			this.sendToIOUnit(ClientConfig.HMACNOTEQUAL);
		}
	}

	@Override
	public void notifyConfirmed() {
		confirmationSent = false;
		this.sendToIOUnit(ClientConfig.GROUPBIDCONFIRMED);
	}

	/*------------------- Network Messaging --------------------*/
	private void sendToNetwork(String message){
		/* if user is logged in then encryption of the message */
		if(crypt != null)
		{
			message = crypt.encodeMessage(message);
		}
		nwcontrol.sendMessageToNetwork(message);
	}

	/*------------------------IO-Unit--------------------------------_*/
	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) {
		ioReceiver = receiver;
	}

	private void sendToIOUnit( String message ){
		ioReceiver.processInstruction(message);
	}

	/* ---------- Exit management ------------ */
	@Override
	public void registerExitObserver(IExitObserver e) {
		eObservers.add(e);
	}

	@Override
	public void exit() {
		if(loggedIn){
			this.sendToNetwork(currentCommand);
			this.resetLogin();
		}
		nwcontrol.shutDownNetworkConnection();
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


}
