package Client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import common.ChatMessage;
import common.Commands;
import encryption.AES;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Runs the background tasks necessary to give the client side application more useful functionality
 *
 */
public class ClientThread extends Thread {

	private String otherUser;
	private String user;
	private TextArea messagesDisplay;
	
	private Socket clientSocket;
	
	private boolean running;
	private int numMessages = 0;
	private PrintWriter serverSideWriter;
	private ObjectInputStream serverObjectReader;
	private BufferedReader serverSideReader;
	
	AES aes;
	private Text otherUserOnlineStatus;
	private ClientApp client;


	public ClientThread(ClientApp client, String user, String otherUser, TextArea messagesDisplay, Text otherUserOnlineStatus) {
		
		try {
			this.client = client;
			this.user = user;
			this.otherUser = otherUser;
			this.messagesDisplay = messagesDisplay;
			this.clientSocket = client.getClientSocket();
			this.serverSideWriter = client.getServerSideWriter();
			this.serverObjectReader = client.getServerObjectReader();
			this.serverSideReader = client.getServerSideReader();
			this.otherUserOnlineStatus = otherUserOnlineStatus;
			
			this.running = true;
			
            //The AES object will load in the common key
            aes = new AES();
            aes.loadKey("./src/encryption/commonKey.key");
            //System.out.println("Encryption key loaded on MsgDisplayThread");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Will listen for communication from the server and execute any relevant requests and then update conversation thread from database
	 * and update the online status of the person that the user wants to communicate with
	 */
	public void run() {
		
		while (running) {
						
			try {
				
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						
							listenToServer();
								
							getMessagesStringVer(user, otherUser, messagesDisplay);	
							
							getOnlineStatus(otherUser, otherUserOnlineStatus);
						
					}	
				});
				
				//I've noticed some strange BufferedReader issues if I don't make the thread sleep for long enough
				//Especially when retrieving from a cloud based database.
				Thread.sleep(3000); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Currently the only thing it listens for is a prod alert command
	 */
	private void listenToServer() {
		
		try {
						
			if (serverSideReader.ready()) {
				
			String serverRequest = serverSideReader.readLine();
			
			//System.out.println("Client heard " + serverRequest); //debugging
			
				if (serverRequest.equals(Commands.PROD)) {
					
					String sender = serverSideReader.readLine();
					String receiver = serverSideReader.readLine();
					
					sender = aes.decryptString(sender);
					receiver = aes.decryptString(receiver);
					
					Alert prodded = new Alert(AlertType.INFORMATION);
					
//					Some unreliability issues with this where it sometimes get them the wrong way around
//					Due to the nature of the bufferedReader(???) simplified version below if you need it
					prodded.setHeaderText("Congratulations " + receiver);
					prodded.setContentText(sender + " sent you a prod!");
					
//					prodded.setHeaderText("Congratulations");
//					prodded.setContentText("You have been prodded");
					
					prodded.show();
				}
				else {
					
					/*
					 * I've noticed that in this context my buffer starts going out of sync and the serverrequest starts
					 * reading in the wrong things. I've written this code to try to clear it
					 * 
					 * Then run the prod command again
					 */
					while (serverSideReader.ready()){
						serverSideReader.readLine();
						//System.out.println("Clearing buffer"); //debugging code/
						
					}
					
					client.prod(user, otherUser);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Changes a condition such that the thread will stop running.
	 */
	public void cancelThread() {
		
		this.running = false;
	}
	

	/**
	 * Requests all conversation data between the two given users, de-crypts the data and updates the messagesdisplay area
	 *  
	 *  
	 *  
	 * @param user
	 * @param otherUser
	 * @param messagesDisplay
	 */
	public void getMessagesStringVer(String user, String otherUser, TextArea messagesDisplay) {
		
		//Send the request details to the server, sends uers details in an encrypted manner
		serverSideWriter.println(Commands.GETMESSAGESSTRINGVER);
		serverSideWriter.println(aes.encryptedString(user));
		serverSideWriter.println(aes.encryptedString(otherUser));
		
		
		try {

			//Builds up an arraylist of chat messages received from the server
			ArrayList<ChatMessage> allMessages = new ArrayList<ChatMessage>();

			if (serverSideReader.readLine().equals(Commands.MESSAGESINCOMING)) {

				//The first readLine at this stage will either be the sender name or the nomoremessages command
				//After each chat message is read, that above logic applies again.
				String reader = "";
				while (!(reader = serverSideReader.readLine()).equals(Commands.NOMOREMESSAGES)) {

					String encSender = reader;
					String encReceiver = serverSideReader.readLine();
					String encMessage = serverSideReader.readLine();
					String encTimeStamp = serverSideReader.readLine();
					
					String sender = aes.decryptString(encSender);
					String receiver = aes.decryptString(encReceiver);
					String message = aes.decryptString(encMessage);
					String timeStamp = aes.decryptString(encTimeStamp);
					
					
					allMessages.add(new ChatMessage(sender, receiver, message, timeStamp));
				}

				//If the number of messages is different from what the thread previously had, then it's worth 
				//updating the GUI
				if (allMessages.size() != numMessages) {

					messagesDisplay.clear();

					for (ChatMessage m : allMessages) {

						String formattedmsg = m.chatFormat(user);
						messagesDisplay.appendText(formattedmsg + "\n");
					}

					numMessages = allMessages.size(); //Update the tracker
					messagesDisplay.setScrollTop(Double.MAX_VALUE); //scroll to the bottom
				}
				
				//Not sure if this code is helpful, might help reduce some bugs related to the reader that I experienced
				while (serverSideReader.ready()){
					serverSideReader.readLine();
					System.out.println("Clearing buffer");
					
				}	
			}
			
		} catch (NullPointerException e) {
		
			
		}
		catch (SocketException e) {
			
			this.cancelThread();
			System.out.println("Thread cancel command sent");

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * Updates the GUI with the online status of the user that you are interacting with
	 * @param otherUser
	 * @param otherUserOnlineStatus
	 */
	public void getOnlineStatus(String otherUser, Text otherUserOnlineStatus) {
		
		try {
			serverSideWriter.println(Commands.CHECKIFONLINE);
			serverSideWriter.println(aes.encryptedString(otherUser));
			
			String response = serverSideReader.readLine();
			
			if (response.equals(Commands.ISONLINE)) {
				
				otherUserOnlineStatus.setText(Commands.currentlyOnlineText);
				otherUserOnlineStatus.setFill(Color.GREEN);
			}
			else if (response.equals(Commands.ISOFFLINE)) {
				
				otherUserOnlineStatus.setText(Commands.currentlyOfflineText);
				otherUserOnlineStatus.setFill(Color.RED);
			}
			
			//Again...this code is here just in case
			while (serverSideReader.ready()){
				serverSideReader.readLine();
				System.out.println("Clearing buffer");
				
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
