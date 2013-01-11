package auction.global.io;

import auction.global.interfaces.IExitObserver;
import auction.global.interfaces.IExitSender;
import auction.global.interfaces.ILocalMessageReceiver;

public class IOUnit 
implements IOInstructionReceiver, IExitObserver{

	private ILocalMessageReceiver localMessenger = null;
	private IOInstructionSender clientModel = null; 
	private OutputSystem output = null;
	private InputSystem input = null;

	public IOUnit(ILocalMessageReceiver rcv, IOInstructionSender instructionSender, IExitSender s, String welcomeMessage) {
		localMessenger = rcv;
		clientModel = instructionSender;
		clientModel.registerIOReceiver(this);
		s.registerExitObserver(this);

		output = new OutputSystem(welcomeMessage);
		input = new InputSystem( this );
	}

	@Override
	public void processInstruction(String instruction) {
		sendToOutput(instruction);
	}

	private void sendToOutput(String instruction) {
		output.receiveInstruction( instruction );
	}

	public void sendMessage(String message) {
		sendToLocalMessenger(message);
	}

	private void sendToLocalMessenger(String message) {
		localMessenger.receiveLocalMessage(message);
	}

	@Override
	public void exit() {
		input.closeInput();
		output.closeOutput();
	}

	@Override
	public String performInput() {
		return input.getInput();
	}

}
