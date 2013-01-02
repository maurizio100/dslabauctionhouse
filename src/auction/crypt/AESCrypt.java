package auction.crypt;


import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

public class AESCrypt implements Crypt{

	
	private SecureRandom iv;
	private Key secretKey;
	public AESCrypt(String key, String iv)
	{
		//TODO initialize <secret-key> and <iv-parameter>
		this.secretKey = new SecretKeySpec(key.getBytes(), "AES");
		this.iv = new SecureRandom(iv.getBytes());
	}
	
	@Override
	public String encodeMessage(String message) {
		
		byte[] cryptmessage = null; 
		Cipher c;
		try {
			c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, secretKey, iv);
			cryptmessage = c.doFinal(message.getBytes());
			cryptmessage = Base64.encode(cryptmessage);
			return cryptmessage.toString();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String decodeMessage(String message) {
		byte[] cryptmessage = null; 
		Cipher c;
		try {
			c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, secretKey, iv);
			cryptmessage = c.doFinal(message.getBytes());
			cryptmessage = Base64.encode(cryptmessage);
			return cryptmessage.toString();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public boolean check(byte[] number) {
		// TODO Auto-generated method stub
		return false;
	}
}
