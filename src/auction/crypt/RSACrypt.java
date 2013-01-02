package auction.crypt;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.PublicKey; 

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;


public class RSACrypt implements Crypt{

	private final byte[] secureNumber = new byte[32];
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public RSACrypt(String pathToPublicKey, PrivateKey privateKey) throws IOException
	{
		new SecureRandom().nextBytes(secureNumber);
		//TODO Private Key vom Clien und Public Key vom Server müssen noch initialisert werden
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		this.publicKey = (PublicKey) in.readObject();
		this.privateKey = privateKey;
		
	}
	
	@Override
	public String encodeMessage(String message) {
		
		//Nachricht wird verschlüsselt und danach in Base64 umgewandelt
		try {
			message = message + " " + secureNumber;
			byte[] cryptmessage = null; 
			
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, publicKey);
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
		
		//Nachricht wird verschlüsselt und danach in Base64 umgewandelt
		try {
			byte[] cryptmessage = message.getBytes(); 
			
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, privateKey);
			cryptmessage = Base64.decode(cryptmessage);
			cryptmessage = c.doFinal(cryptmessage);
			
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
	public boolean check(byte[] s)
	{
		if(secureNumber.equals(s))
		{
			return true;
		}
		return false;
	}

}
