package auction.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

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
import auction.commands.OkCommand;
import auction.commands.OverbidCommand;
import auction.communication.ExitObserver;
import auction.communication.ExitSender;
import auction.communication.MessageReceiver;
import auction.communication.MessageSender;
import auction.crypt.AESCrypt;
import auction.crypt.Crypt;
import auction.crypt.RSACrypt;
import auction.exceptions.NotACommandException;
import auction.io.IOInstructionReceiver;
import auction.io.IOInstructionSender;
import auction.io.IOUnit;

public class ClientModel 
implements MessageReceiver, IOInstructionSender, ExitSender, AuctionCommandReceiverClient, ClientCommandReceiver, AuctionCommandReceiverServer{

	private MessageSender localMessenger = null;
	private MessageReceiver networkMessenger = null;
	private IOInstructionReceiver ioReceiver = null;
	private ArrayList<ExitObserver> eObservers = null;
	private CommandRepository commandRepository = null;
	private boolean loggedIn = false;
	private String currentCommand = "";
	private String[] splittedString;
	private int udpPort = -1;
	private String pathToPublicKey = null;
	private String pathToPrivateKey = null;
	private Crypt crypt = null;
	
	private Command[] availableCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new OverbidCommand(this),
			new AuctionEndedCommand(this),
			new OkCommand(this)
	};
	
	public ClientModel(MessageSender lmc,
			MessageReceiver nmfc, int udpPort) {
		
		localMessenger = lmc;
		networkMessenger = nmfc;
		
		localMessenger.registerMessageReceiver(this);
		eObservers = new ArrayList<ExitObserver>();
		
		commandRepository = new CommandRepository(availableCommands);
		this.udpPort = udpPort;
	}
	
	public ClientModel(MessageSender lmc,
			MessageReceiver nmfc, int udpPort, String pathToPublicKey, String pathToPrivateKey) {
		this(lmc, nmfc, udpPort);
		this.pathToPrivateKey = pathToPrivateKey;
		this.pathToPublicKey = pathToPublicKey;
	}

	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) {
		ioReceiver = receiver;
	}

	@Override
	public void receiveMessage(String message) {
		parseMessage(message);
	}

	private void parseMessage(String message) {
		//Nachricht entschlüsseln
		if(crypt != null)
		{
			message = crypt.decodeMessage(message);
		}
		
		if( this.isCommand(message) ){
			try{ 
				Command c = parseCommand(message);
				currentCommand = message;
				c.execute();
				currentCommand = null;
			}catch( NotACommandException nace ){
				ioReceiver.receiveInstruction(nace.getMessage());
			}
		}else{
			sendToIOUnit(message);
		}
		
	}

	private Command parseCommand(String command) throws NotACommandException{
		splittedString = command.split(" ");
		Command c = commandRepository.checkCommand(splittedString[0]);
		if ( c == null ) throw new NotACommandException(splittedString[0] + " is not a command!");
		
		return c;
	}

	private synchronized void sendToNetwork(String message){
		
		//falls Crypt != null mit Crypt verschlüsseln//
		if(crypt != null)
		{
			message = crypt.encodeMessage(message);
		}
		networkMessenger.receiveMessage(message);
	}
	
	private synchronized void sendToIOUnit( String message ){
		ioReceiver.receiveInstruction(message);
	}
	
	private void sendSyntaxError( String command, String correctSyntax){
		String message = "Syntaxerror in Command: " + command + "\n";
		message += "Correct usage: " + correctSyntax;
		
		sendToIOUnit(message);
	}

	private boolean isCommand(String message) {
		if( message.length()  < 1 ) return false;
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
		if( loggedIn ){
			this.sendToIOUnit("You are still logged in!");
			return;
		}
		
		if(splittedString.length != 2){ 
			this.sendSyntaxError(splittedString[0], "!login <username>");
			return;
		}

//		ioReceiver.setUser(splittedString[1]);
		this.sendToNetwork(currentCommand + " " + udpPort);
		
		//Passwortabfrage für Private Key
		PEMReader in;
		try {
			pathToPrivateKey += splittedString[1]+".pem";
			in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
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
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		loggedIn = true; --> bei !ok befehl
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

		this.sendToNetwork(currentCommand);
		ioReceiver.resetUser();
		loggedIn = false;
	}

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
	public void exit() {
		this.sendToNetwork(currentCommand);
		if(loggedIn){
			loggedIn =false;
			ioReceiver.resetUser();
		}
		this.sendExit();
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

	@Override
	public void invokeShutdown() {
		this.sendExit();
	}

	@Override
	public void ok() {
		splittedString = currentCommand.split(" ", 5);
		
		//wenn clientchallenge passt dann
		if(crypt.check(splittedString[1].getBytes()))
		{
			crypt = new AESCrypt(splittedString[3], splittedString[4]);
			this.sendToNetwork(splittedString[2]);
			loggedIn = true;
		}
		else
		{
			this.sendToIOUnit("Login Failed!");
		}
	}
	
}
