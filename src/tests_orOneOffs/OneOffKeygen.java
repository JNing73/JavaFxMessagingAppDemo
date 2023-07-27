package tests_orOneOffs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import encryption.AES;

/**
 * In this assignment I'm just generating a symmetric key to be used by the client and server. No reason to keep regenerating.
 *
 */
class OneOffKeygen {

	public OneOffKeygen() {
		
		try {
			AES aesObject = new AES();
            aesObject.saveKey("./src/encryption/commonKey.key"); //Save the key

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main (String[] args) {
		
		new OneOffKeygen();
	}
}
