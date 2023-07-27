package Client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import common.ChatMessage;
import common.Commands;
import encryption.AES;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

/**
 * This is the logic for the client application which the UI calls upon to communicate with the server
 * 
 * 
 *
 */
public class ClientApp {
	
	private Socket clientSocket;
	private PrintWriter serverSideWriter;
	private BufferedReader serverSideReader;
	private ObjectOutputStream serverObjectWriter;
	private ObjectInputStream serverObjectReader;
	
	boolean successfulHandshake = false;
	
	String activeUsername;
	
	AES aes;
	
	//This counter is used to help detect when the number of messages returned by the database query has changed
	int numMessages = 0;
	private ClientThread clientThread;

	
	public ClientApp(String serverAddress, int port) {
		
		
		try {
			//Create a client socket which will connect to the specified server on the specified port
			clientSocket = new Socket(serverAddress, port);
			
			//Setup a buffered reader to read from the server
			serverSideReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			//Setup a print writer to send things to the server
			serverSideWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			
			//This will let us send objects to server
            serverObjectWriter = new ObjectOutputStream(clientSocket.getOutputStream());
            
            //This will let us read objects sent from the server
            serverObjectReader  = new ObjectInputStream(clientSocket.getInputStream());

            //The DES object will load in the common key
            aes = new AES();
            aes.loadKey("./src/encryption/commonKey.key");
            //System.out.println("Encryption key loaded on client side end");
            					
			//Make yourself known to the server UI
			introduce();
						
			
		} 
		catch (ConnectException e) {
			
			Alert connectException = new Alert(AlertType.ERROR);
			connectException.setContentText("Server may be unavailable or you have entered the wrong details");
			connectException.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Sends a request along with the necessary data to construct a connected but not logged in user on the receiving end
	 * @throws IOException 
	 */
	public void introduce() throws IOException {
		
		serverSideWriter.println(Commands.HANDSHAKE);
		String clientIP = "";
				
		try { 
		String urlString = "http://checkip.amazonaws.com/";
		URL url = new URL(urlString);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		clientIP = br.readLine();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//clientIP = clientSocket.getLocalAddress().getHostAddress();
		
		serverSideWriter.println(clientIP);
		
		String response = serverSideReader.readLine();
		if (response.equals(Commands.HANDSHAKEGOOD)) {
			successfulHandshake = true;
			
		}
	}

	
	
	public void disconnectRequest() {
		
			serverSideWriter.println(Commands.DISCONNECT);
	}
	
	

	public boolean login(String uname, String pwd) {
		
		try {
			
			//Run the encryption
			uname = aes.encryptedString(uname);
			pwd = aes.encryptedString(pwd);
			
			
			//Send login request and details
			serverSideWriter.println(Commands.LOGIN);
			serverSideWriter.println(uname);
			serverSideWriter.println(pwd);
			
			//Get reply from server
			String response = serverSideReader.readLine();
			
			if (response.equals(Commands.LOGINOK)) {
				
				//Get back the exact username stored in the database (since login is not case sensitive
				//but we want to present the user with what they typed in when they registered)
				String encryptedUsername = serverSideReader.readLine();
				
				activeUsername = aes.decryptString(encryptedUsername);
				
				return true;
			}
			else {return false;}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	
	public void logout() {
		
		serverSideWriter.println(Commands.LOGOUT); //Tell server you're logging out
		activeUsername = ""; //No active user anymore
		this.stopExistingThread(); //Stop the thread if there is one
	}

	
	/**
	 * 
	 * Registers user to the database. Encrypted communication
	 * 
	 * @param newUsername
	 * @param newPassword
	 * @param fname
	 * @param lname
	 * @return
	 */
	public boolean registerRequest(String newUsername, String newPassword, String fname, String lname) {
		
		serverSideWriter.println(Commands.REGISTER);
		
		
		newUsername = aes.encryptedString(newUsername);
		newPassword = aes.encryptedString(newPassword);
		fname = aes.encryptedString(fname);
		lname = aes.encryptedString(lname);
		
		serverSideWriter.println(newUsername);
		serverSideWriter.println(newPassword);
		serverSideWriter.println(fname);
		serverSideWriter.println(lname);
		
		String response;
		try {
			response = serverSideReader.readLine();
			
			if (response.equals(Commands.REGISTEROK)) {
				
				return true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	
	
	/**
	 * The name of the person who is logged in
	 * @return
	 */
	public String getActiveUsername() {
		return activeUsername;
	}

	
	/** 
	 * Returns a list of all the users registered in the database, since in the current implementation
	 * all users can interact with all other users
	 */
	public ArrayList<String> getAllUsers() {
		
		serverSideWriter.println(Commands.GETUSERLIST);
		
		ArrayList<String> users = new ArrayList<String>();
		
		try {
			String response = serverSideReader.readLine();
			
			while (!response.equals(Commands.DONE)) {
				
				users.add(response);
				response = serverSideReader.readLine();
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return users;
	}
	
	
	/**
	 * Starts the client thread 
	 * 
	 * @param text
	 * @param otherUser
	 * @param messagesDisplay
	 * @param otherUserOnlineStatus
	 */
	public void startMessageThread(String text, String otherUser, TextArea messagesDisplay, Text otherUserOnlineStatus) {
		
		clientThread = new ClientThread(this, text, otherUser, messagesDisplay, otherUserOnlineStatus);
		clientThread.start();
		
	}

	/**
	 * Stops an existing thread if there is one running
	 */
	public void stopExistingThread() {
		
		if (clientThread != null) {
		
			clientThread.cancelThread();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			clientThread = null;
		}
	}
	

	/**
	 * Sends a message to the database with the given parameters
	 * 
	 * @param sender
	 * @param receiver
	 * @param messageToSend
	 * @param timestamp
	 * @param messagesDisplay
	 */
	public void sendAmessage(String sender, String receiver, String messageToSend, String timestamp) {
		
		serverSideWriter.println(Commands.SENDMESSAGE);
		
		serverSideWriter.println(sender);
		serverSideWriter.println(receiver);
		serverSideWriter.println(messageToSend);
		serverSideWriter.println(timestamp);
		
	}


	/**
	 * Sends a request to the server to send a prod alert to receiver
	 * 
	 * @param sender
	 * @param receiver
	 */
	public void prod(String sender, String receiver) {
		
		serverSideWriter.println(Commands.PROD);
		serverSideWriter.println(aes.encryptedString(sender));
		serverSideWriter.println(aes.encryptedString(receiver));
	
	}

	
	//Other getters and setters

	public Socket getClientSocket() {
		return clientSocket;
	}


	public PrintWriter getServerSideWriter() {
		return serverSideWriter;
	}


	public BufferedReader getServerSideReader() {
		return serverSideReader;
	}


	public ObjectOutputStream getServerObjectWriter() {
		return serverObjectWriter;
	}


	public ObjectInputStream getServerObjectReader() {
		return serverObjectReader;
	}


	public boolean isSuccessfulHandshake() {
		return successfulHandshake;
	}


	public AES getAes() {
		return aes;
	}


	public int getNumMessages() {
		return numMessages;
	}


	public ClientThread getMessageFetchingThread() {
		return clientThread;
	}


	
}
