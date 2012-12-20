package auction.client;

import java.util.ArrayList;

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
import auction.communication.ExitObserver;
import auction.communication.ExitSender;
import auction.communication.MessageReceiver;
import auction.communication.MessageSender;
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
	
	private Command[] availableCommands = {
			new BidAuctionCommand(this),
			new CreateAuctionCommand(this),
			new ExitCommand(this),
			new ListCommand(this),
			new LoginCommand(this),
			new LogoutCommand(this),
			new OverbidCommand(this),
			new AuctionEndedCommand(this)
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

	@Override
	public void registerIOReceiver(IOInstructionReceiver receiver) {
		ioReceiver = receiver;
	}

	@Override
	public void receiveMessage(String message) {
		parseMessage(message);
	}

	private void parseMessage(String message) {
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

		ioReceiver.setUser(splittedString[1]);
		this.sendToNetwork(currentCommand + " " + udpPort);
		loggedIn = true;
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
	
}
