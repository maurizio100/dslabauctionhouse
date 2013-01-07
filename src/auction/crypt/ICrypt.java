package auction.crypt;

public interface ICrypt {

	public String encodeMessage(String message);
	public String decodeMessage(String message);
}
