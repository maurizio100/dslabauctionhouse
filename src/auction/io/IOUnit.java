package auction.io;

import auction.communication.ExitObserver;
import auction.communication.ExitSender;
import auction.communication.MessageReceiver;

public class IOUnit implements IOInstructionReceiver, MessageReceiver, ExitObserver{

	private MessageReceiver localMessenger = null;
	private IOInstructionSender clientModel = null; 
	private OutputSystem output = null;
	private InputSystem input = null;
	
	
	public IOUnit(MessageReceiver rcv, IOInstructionSender model, ExitSender s, String welcomeMessage) {
		localMessenger = rcv;
		clientModel = model;
		clientModel.registerIOReceiver(this);
		s.registerExitObserver(this);
		
		output = new OutputSystem(welcomeMessage);
		input = new InputSystem( this );
		
	}

	@Override
	public void receiveInstruction(String instruction) {
		sendToOutput(instruction);
	}

	private void sendToOutput(String instruction) {
		output.receiveInstruction( instruction );
	}

	@Override
	public void receiveMessage(String message) {
		sendToLocalMessenger(message);
	}

	private void sendToLocalMessenger(String message) {
		localMessenger.receiveMessage(message);
	}

	@Override
	public void exit() {
		input.closeInput();
		output.closeOutput();
		
	}

	@Override
	public void resetUser() {
		output.resetUser();
	}

	@Override
	public void setUser(String user) {
		output.setUser(user);
	}

	@Override
	public void invokeShutdown() {
		localMessenger.invokeShutdown();
		
	}

	@Override
	public String performInput() {
		return input.getInput();
	}
	
}
