package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import Client.User;
import common.ChatMessage;
import common.Commands;
import encryption.AES;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import serverUtilities.ServerConsole;

public class ServerThread extends Thread {
	
	
	
	//The thread needs to know which client socket it is serving
	private Socket clientSocket;
	private User activeUser;

	private Connection sqlConnection;

	private TextArea statusConsole;
	private ObservableList<User> connectedUsers;
	private HashMap<String,Socket> connectedUsersMap;

	
	private BufferedReader clientSideReader;
	private PrintWriter clientSideOutput;
	
	private ObjectOutputStream clientObjectWriter;
	private ObjectInputStream clientObjectReader;
	
	//Encryption
	AES aes;
	
	
	
	
	public ServerThread(Connection sqlConnection, ObservableList<User> connectedUsers, HashMap<String,Socket> connectedUsersMap, TextArea statusConsole, Socket clientSocket) {
		
		this.clientSocket = clientSocket;
		this.statusConsole = statusConsole;
		this.connectedUsers = connectedUsers;
		this.sqlConnection = sqlConnection;
		this.connectedUsersMap = connectedUsersMap;
		
		try {
			
			aes = new AES();
			aes.loadKey("./src/encryption/commonKey.key");
			//System.out.println("encryption object loaded at server thread");
		}
		catch (Exception e) {	
			e.printStackTrace();
		}
	}
	
	
	
	public void run() {
		
		try {
			//This will let us read from the 
			clientSideReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			//This will let us send things to the client
			clientSideOutput = new PrintWriter(clientSocket.getOutputStream(), true);
			
			//This will let us send objects to client
			clientObjectWriter = new ObjectOutputStream(clientSocket.getOutputStream());
			
			//This will let us read objects sent from the client
			clientObjectReader  = new ObjectInputStream(clientSocket.getInputStream());

			//This will listen for requests from the client side and run the appropriate method
			while (true) {
				
				
				if (clientSideReader.ready()) {
					
					String command = clientSideReader.readLine();
					
					
					
					if (command.equals(Commands.HANDSHAKE)) {
						
						handshake();
					}
					else if (command.equals(Commands.DISCONNECT)) {
						
						disconnect();
					}
					else if (command.equals(Commands.LOGIN)) {
						
						login();
					}
					else if (command.equals(Commands.LOGOUT)) {
						
						logout();
					}
					else if (command.equals(Commands.REGISTER)) {
						
						register();
					}
					else if (command.equals(Commands.GETUSERLIST)) {
						giveUserList();
					}
					else if (command.equals(Commands.SENDMESSAGE)) {
						
						sendMessage();
					}
					else if (command.equals(Commands.GETMESSAGESSTRINGVER)) {
						
						getMessagesStringVer();
					}
					else if (command.equals(Commands.CHECKIFONLINE)) {
						
						checkOnlineStatus();
					}
					else if (command.equals(Commands.PROD)) {
						
						prod();
					}

				}
				
				//Thread.sleep(2000); //To stop my computer from needlessly overworking I hope
			}
			
			
			//In this case we've finished the job, so close the connection
			//clientSocket.close();
		}
		catch(IOException e) {
			System.out.println("Error");
			e.printStackTrace();}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Sends a prod request to another client logged into the server
	 */
	private void prod() {
		
		try {
			
			String encSender = clientSideReader.readLine();
			String encReceiver = clientSideReader.readLine();
			
			String receiver = aes.decryptString(encReceiver);
			
			Socket receiverSocket = connectedUsersMap.get(receiver);
			
			PrintWriter receiverSideOutput = new PrintWriter(receiverSocket.getOutputStream(), true);
		
			receiverSideOutput.println(Commands.PROD);
			receiverSideOutput.println(encSender);
			receiverSideOutput.println(encReceiver);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}


	/**
	 * Checks whether a specific client is online 
	 * 
	 * @throws IOException
	 */
	private void checkOnlineStatus() throws IOException {
		
		String otherUser = clientSideReader.readLine();
		otherUser = aes.decryptString(otherUser);
		
		//Check the hashmap of connected users
		if (connectedUsersMap.containsKey(otherUser)) {
			
			clientSideOutput.println(Commands.ISONLINE);
		}
		else {
			
			clientSideOutput.println(Commands.ISOFFLINE);
		}
		
	}


	//Sends a message to the database
	//Not encrypted send to the database just because I'd have to redo all the fake conversations I made on my database
	//If I were to do this again, obviously I'd do that
	private void sendMessage() throws IOException, SQLException {
		
		String sender = clientSideReader.readLine();
		String receiver = clientSideReader.readLine();
		String message = clientSideReader.readLine();
		String timestampString = clientSideReader.readLine();
				
		String query = "INSERT INTO `Messages` (`SenderUName`, `ReceiverUName`, `Message`, `Timestamp`) VALUES (?, ?, ?, ?)";
		
		PreparedStatement preparedStatement = sqlConnection.prepareStatement(query);
		preparedStatement.setString(1, sender);
		preparedStatement.setString(2, receiver);
		preparedStatement.setString(3, message);
		preparedStatement.setString(4, timestampString);
		
		preparedStatement.executeUpdate();
	}


	
	/**
	 * A get messages implementation that just sends data as strings, as opposed to the object version which would have sent
	 * an arraylist object.
	 * 
	 * The user data is encrypted
	 * 
	 * @throws IOException
	 * @throws SQLException
	 */
	private void getMessagesStringVer() throws IOException, SQLException {
		
		//Pull all the relevant messages from the server
		String senderUname = clientSideReader.readLine();
		String receiverUname = clientSideReader.readLine();
		
		senderUname = aes.decryptString(senderUname);
		receiverUname = aes.decryptString(receiverUname);
		
		String query = "SELECT * FROM `Messages` WHERE SenderUName = ? AND ReceiverUName = ? OR SenderUName = ? AND ReceiverUName = ? ORDER BY `Messages`.`Timestamp` ASC;";
		
		PreparedStatement ps = sqlConnection.prepareStatement(query);
		ps.setString(1, senderUname);
		ps.setString(2, receiverUname);
		ps.setString(3, receiverUname);
		ps.setString(4, senderUname);
		
		ResultSet res = ps.executeQuery();	
		
		clientSideOutput.println(Commands.MESSAGESINCOMING);
		
		while (res.next()) {
			
			String sender = res.getString("SenderUName");
			String receiver = res.getString("ReceiverUname");
			String message = res.getString("Message");
			String timeStamp = res.getString("Timestamp");
			
			//Encrypts them before sending back to the client
			String encSender = aes.encryptedString(sender);
			String encReceiver = aes.encryptedString(receiver);
			String encMessage = aes.encryptedString(message);
			String encStamp = aes.encryptedString(timeStamp);
			
			clientSideOutput.println(encSender);
			clientSideOutput.println(encReceiver);
			clientSideOutput.println(encMessage);
			clientSideOutput.println(encStamp);
		}
		
		clientSideOutput.println(Commands.NOMOREMESSAGES);
		
		
	}
	


	private void giveUserList() throws SQLException {
		
		//Query to check if that username already exists
		String userListQuery = String.format("SELECT * FROM `users`");
		Statement s = sqlConnection.createStatement();
		ResultSet res = s.executeQuery(userListQuery);
		
		//write the results into the printwriter
		while (res.next()) {
			clientSideOutput.println(res.getString("username"));
		}
		
		clientSideOutput.println(Commands.DONE);
		
	}



	private void disconnect() throws IOException {
		
		/*
		 * Close the connection, remove the user from the users list, print a message on the server console
		 */
		clientSocket.close();
		connectedUsers.remove(activeUser);
		ServerConsole.message(statusConsole, activeUser.getIp() + " has disconnected");
	}

	
	/**
	 * Represents a client <-> server connection but NOT logged in yet
	 * 
	 * The server will be told the ip of the client connected via a message and on the table
	 * 
	 * @throws IOException
	 */
	private void handshake() throws IOException {
		
		String ip = clientSideReader.readLine();
		
		activeUser = new User(ip, clientSocket.getPort());
		connectedUsers.add(activeUser);
				
		clientSideOutput.println(Commands.HANDSHAKEGOOD);
		
		ServerConsole.message(statusConsole, (ip + " (port: " + clientSocket.getPort() + ") has connected"));
	}
	
	/**
	 * Logs the user in if the username and password are correct
	 * 
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	private void login() throws SQLException, IOException {
		
		String username = clientSideReader.readLine();
		String password = clientSideReader.readLine();
		
		username = aes.decryptString(username);
		password = aes.decryptString(password);
		
		String query = "SELECT * FROM `users` WHERE username = ? AND password = ?";
        PreparedStatement ps = sqlConnection.prepareStatement(query);
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet res = ps.executeQuery();
        
        //Our expectation of a valid login is that only one user is connected
        int count = 0;
        String userName = "";
        while (res.next()) {
        	
        	userName = res.getString("username");
        	count++;
        	
        	if (count > 1) {break;} //no point continuing to count
        }
        
        if (count == 1) {
        	
        	clientSideOutput.println(Commands.LOGINOK);
        	
        	//Swap out the active user for a more complete version of the active user
        	//Unfortunately just changing the properties of the active user doesn't update the GUI table itself...
        	String ipAddress = activeUser.getIp();
        	connectedUsers.remove(activeUser);     	
        	activeUser = new User(ipAddress, userName, clientSocket.getPort());
        	
        	/*
        	 * Inform the server via console, add into the connectedUsers list so UI table displays the user
        	 * Add them to the hashmap too for later reference
        	 */
        	ServerConsole.message(statusConsole, activeUser.getIp() + " has logged in as " + activeUser.getUsername());
        	connectedUsers.add(activeUser);
        	connectedUsersMap.put(username, clientSocket);
        	
        	//System.out.println(connectedUsersMap.get(activeUser.getUsername()).toString());
        	
        	String encryptedUname = aes.encryptedString(userName);
        	clientSideOutput.println(encryptedUname); 
        }
        else {
        	
        	clientSideOutput.println(Commands.LOGINFAIL);
        }
		
	}
	
	private void logout() {
		
    	//Swap out the logged in active user for non-logged in version of itself
    	//Unfortunately just changing the properties of the active user doesn't update the GUI table itself...
    	String ipAddress = activeUser.getIp();
    	String uname = activeUser.getUsername();//grab the username before we replace it
    	connectedUsers.remove(activeUser);     	
    	activeUser = new User(ipAddress, clientSocket.getPort());
    	connectedUsers.add(activeUser);
    	
    	//But we want to completely remove them from the hashmap since that user should no longer
    	//be contacted in a user -> server -> user2 manner
    	connectedUsersMap.remove(uname);
    	
    	
    	ServerConsole.message(statusConsole, uname + " has logged out");
		
	}



	private void register() throws IOException, SQLException {
		
		
		String newUsername = clientSideReader.readLine();
		String newPassword = clientSideReader.readLine();
		String fname = clientSideReader.readLine();
		String lname = clientSideReader.readLine();
		
		newUsername = aes.decryptString(newUsername);
		newPassword = aes.decryptString(newPassword);
		fname = aes.decryptString(fname);
		lname = aes.decryptString(lname);
		
		//Query to check if that username already exists
		String duplicateNameQuery = "SELECT * FROM `users` WHERE username = ? ";
		PreparedStatement ps = sqlConnection.prepareStatement(duplicateNameQuery);
		ps.setString(1, newUsername);
		ResultSet res = ps.executeQuery();
		
		//We hoping for NO result, meaning that username is available
		if (!(res.next())) {
			
			String insertNewUserQuery = "INSERT INTO `users` (`username`, `password`, `fname`, `lname`) VALUES (?, ?, ?, ?);";
			PreparedStatement ps2 = sqlConnection.prepareStatement(insertNewUserQuery);
			ps2.setString(1, newUsername);
			ps2.setString(2, newPassword);
			ps2.setString(3, fname);
			ps2.setString(4, lname);
			
			ps2.executeUpdate();
			
			clientSideOutput.println(Commands.REGISTEROK);
		}
		else {
			clientSideOutput.println(Commands.REGISTERFAIL);
		}
		
	}

}