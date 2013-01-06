package auction.crypt;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.encoders.Hex;


public class HMAC {
	byte[] keyBytes = new byte[1024];
	Key key = null;
	public HMAC(String pathToSecretKey, String username) throws IOException
	{
		
		FileInputStream fis = new FileInputStream(pathToSecretKey);
		fis.read(keyBytes);
		fis.close();
		byte[] input = Hex.decode(keyBytes);

		key = new SecretKeySpec(input,"HmacSHA256");
	}
	
	public String createHMAC(String message) throws NoSuchAlgorithmException, InvalidKeyException
	{

		Mac hMac = Mac.getInstance("HmacSHA256"); 
		hMac.init(key);
		hMac.update(message.getBytes());
		byte[] hash = hMac.doFinal();
		return hash.toString();
	}
	
	public boolean checkHMAC(String decodedmessage, String hashmac) throws NoSuchAlgorithmException, InvalidKeyException
	{

		Mac hMac = Mac.getInstance("HmacSHA256"); 
		hMac.init(key);
		hMac.update(decodedmessage.getBytes());
		byte[] hash = hMac.doFinal();
		
		if(hash.equals(hashmac))
		{
			return true;
		}
		return false;
	}

}
