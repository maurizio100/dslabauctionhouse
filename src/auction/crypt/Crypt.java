package auction.crypt;

public interface Crypt {

	public String encodeMessage(String message);
	public String decodeMessage(String message);
	boolean check(byte[] number);
}
