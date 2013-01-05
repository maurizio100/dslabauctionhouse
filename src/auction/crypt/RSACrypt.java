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

import auction.interfaces.ICrypt;


public class RSACrypt implements ICrypt{

	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public RSACrypt(PrivateKey privateKey)
	{
		this.privateKey = privateKey;
	}
	
	public RSACrypt(String pathToPublicKey, PrivateKey privateKey) throws IOException
	{
		this(privateKey);
		
		//Private Key vom Client und Public Key vom Server m�ssen noch initialisert werden
		PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
		this.publicKey = (PublicKey) in.readObject();
		
		
	}
	
	@Override
	public String encodeMessage(String message) {
		
		//Nachricht wird verschl�sselt und danach in Base64 umgewandelt
		try {
			byte[] cryptmessage = null; 
			
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, publicKey);
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
		} 
		
		return null;
	}

	@Override
	public String decodeMessage(String message) {
		
		//Nachricht wird entschl�sselt und danach in Base64 umgewandelt
		try {
			byte[] cryptmessage = Base64.decode(message);
			
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, privateKey);
			
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
		} 
		
		return null;
	}

}
