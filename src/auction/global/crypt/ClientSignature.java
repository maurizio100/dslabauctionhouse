package auction.global.crypt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;

public class ClientSignature {
	
	
	public String signMessage(String message, PrivateKey privateKey)
	{
		try {
			Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initSign(privateKey);
			signature.update(message.getBytes()); 
			byte[] result = signature.sign();
			result = Base64.encode(result);
			return new String(result);
			
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean verifyMessage(String message, String sig, String pathToPublicKey) throws IOException
	{
		try {
			PEMReader in = new PEMReader(new FileReader(pathToPublicKey)); 
			PublicKey publicKey = (PublicKey) in.readObject();
			byte[] desig = Base64.decode(sig.getBytes());
			Signature signature = Signature.getInstance("SHA512withRSA");
			signature.initVerify(publicKey);
			signature.update(message.getBytes());
			return signature.verify(desig);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
