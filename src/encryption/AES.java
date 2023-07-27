package encryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

//import sun.misc.BASE64Encoder;


/*
 * This code was provided by Ali Ahmed
 * 
 * By the time I submit this, I may or may not have added a few extra things here and there
 * 
 */

/**
 *
 * @author ahmed
 */
public class AES {
    
    private SecretKey secretkey; 
    
    
    public AES() throws NoSuchAlgorithmException 
    {
        generateKey();
    }
    
    
    /**
	* Step 1. Generate a AES key using KeyGenerator 
    */
    
    public void generateKey() throws NoSuchAlgorithmException 
    {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
        this.setSecretkey(keyGen.generateKey());        
    }
    
    /**
     * Saves a key to the system with the given filename
     * 
     * We write the AES secret key to it
     * 
     * @param secretKeyFileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveKey(String secretKeyFileName) throws FileNotFoundException, IOException
    {       
        ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream (secretKeyFileName));
        try 
        {
          oout.writeObject(this.secretkey);
        } 
        finally 
        {
          oout.close();
        }
    }
    
    public void loadKey(String secretKeyFileName) throws FileNotFoundException, IOException, ClassNotFoundException
    {       
        ObjectInputStream in = new ObjectInputStream(new FileInputStream (secretKeyFileName));
        try 
        {
          this.secretkey = (SecretKey)in.readObject();
        } 
        finally 
        {
          in.close();
        }
    }
    
    public byte[] encrypt (String strDataToEncrypt) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {
        Cipher desCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
        desCipher.init(Cipher.ENCRYPT_MODE, this.getSecretkey());
        byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
        byte[] byteCipherText = desCipher.doFinal(byteDataToEncrypt);       
        return byteCipherText;
    }
    
    public String decrypt (byte[] strCipherText) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException
    {        
        Cipher desCipher = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!				
        desCipher.init(Cipher.DECRYPT_MODE, this.getSecretkey());        
        byte[] byteDecryptedText = desCipher.doFinal(strCipherText);        
        return new String(byteDecryptedText);
    }   

    /**
     * @return the secretkey
     */
    public SecretKey getSecretkey() {
        return secretkey;
    }

    /**
     * @param secretkey the secretkey to set
     */
    public void setSecretkey(SecretKey secretkey) {
        this.secretkey = secretkey;
    }
    
    
    //Since I'm mainly using an printwriter and buffered reader, I wanted to send as strings, so just made a few
    //methods for my convenience
    
    //https://howtodoinjava.com/java/array/convert-byte-array-string/
    public String encryptedString(String plainString) {
    	
        try {
			byte[] encodedBytes = this.encrypt(plainString);
			
			String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
			
			return encodedString;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return "Something went wrong with the encryption";    
    }
    
    
    public String decryptString(String encryptedStringText) {
    	
    	try {
	    	byte[] bytes = Base64.getDecoder().decode(encryptedStringText);
	    	
	    	return this.decrypt(bytes);
    	}
    	catch (Exception e) {
    		
    	}
    	
    	return "Uh-oh!";
    }
    
    
}
