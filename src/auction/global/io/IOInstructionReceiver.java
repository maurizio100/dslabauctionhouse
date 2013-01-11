package auction.global.io;

public interface IOInstructionReceiver {

	public void processInstruction(String instruction);
	public String performInput();

}
