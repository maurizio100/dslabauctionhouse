package auction.global.crypt;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

import auction.global.interfaces.ICrypt;


public class AESCrypt implements ICrypt{
	
	private byte[] iv;
	private Key secretKey;
	
	public AESCrypt(Key key, byte[] iv)
	{
		//initialize <secret-key> and <iv-parameter>
		this.secretKey = key;
		this.iv = iv;
	}
	
	@Override
	public String encodeMessage(String message) {
		byte[] cryptmessage = null; 
		Cipher c;
		try {
			c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
			cryptmessage = c.doFinal(message.getBytes());
			cryptmessage = Base64.encode(cryptmessage);
			return new String(cryptmessage);
			
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
		} catch (InvalidAlgorithmParameterException e) {
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
			cryptmessage = Base64.decode(message);
			c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			cryptmessage = c.doFinal(cryptmessage);
			return new String(cryptmessage);
			
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
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
