package auction.io;

public interface IOInstructionReceiver {

	public void receiveInstruction(String instruction);
	public void resetUser();
	public void setUser(String string);
	public String performInput();
}
