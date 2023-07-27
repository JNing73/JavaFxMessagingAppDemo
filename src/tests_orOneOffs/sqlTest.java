package tests_orOneOffs;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import Server.Server;
import common.ChatMessage;

class sqlTest {

	//Confirming that the connection does return an actual connection with the current settings
	//If the method isn't currently static make it static for the test at least
	@Test
	void testEstablishSqlConnection() {
		
		assertNotEquals(null, Server.establishSqlConnection());
	}
	
	
	//Query database with a known username/password combination
	//Database should return just 1 result.
	@Test
	void loginVerificationTest() {
		
		boolean testResult = false;
		
		Connection sqlConnection = Server.establishSqlConnection();
		
		String username = "aliceon";
		String password = "abcd";
		
		try {
			Statement s = sqlConnection.createStatement();
			String query = String.format("SELECT * FROM `users` WHERE username = '%s' AND password = '%s';", username, password);
	        ResultSet res = s.executeQuery(query);
	        
	        int rows = 0;
	        while (res.next()) {
	        	rows++;
//	        	System.out.println(res.getString("username"));
//	        	System.out.println(res.getString("password"));
	        	
	        }
	        if (rows == 1) {
	        	testResult = true;
	        }
		}
		catch (Exception e) {
			
			testResult = false;
		}
		
		assertEquals(true, testResult);
	}
	
	//Should pass if the username is already taken
	@Test
	void duplicateUsernameChecker() {
		
		Boolean pass = false;
		
		try {
			Connection sqlConnection = Server.establishSqlConnection();
			
			String username = "aliceon";//I know Aliceon is already in the database
			
			String query = String.format("SELECT * FROM `users` WHERE username = '%s'", username);
			Statement s = sqlConnection.createStatement();
			ResultSet res = s.executeQuery(query);
			
			//If any result comes back then we have correctly detected a duplicate username
			if (res.next()) {
				pass = true;
			}
		}
		catch(Exception e) {
			pass = false;
		}
		
		assertEquals(true, pass);
	}
	
	/**
	 * Should pass if we query with a username that doesn't exist
	 */
	@Test
	void availableUsernameChecker() {
		
		
		Boolean pass = false;
		
		try {
			Connection sqlConnection = Server.establishSqlConnection();
			
			String username = "Idonotexistpleasedonotmakeausercalledthis";
			
			String query = String.format("SELECT * FROM `users` WHERE username = '%s'", username);
			Statement s = sqlConnection.createStatement();
			ResultSet res = s.executeQuery(query);
			
			//We expect NO result, because there should NOT be any user with that name
			if (!(res.next())) {
				pass = true;
			}
		}
		catch(Exception e) {
			pass = false;
			e.printStackTrace();
		}
		
		assertEquals(true, pass);
	}
	
	@Test
	void basicMessageRetrieval() {
		
		boolean result = false;
		
		Connection sqlConnection = Server.establishSqlConnection();
		
		String senderUname = "Aliceon";
		String receiverUname = "bobward";
		
		String query = String.format("SELECT * FROM `Messages` "
				+ "WHERE SenderUName = '%s' OR senderUName = '%s' AND ReceiverUName = '%s' OR ReceiverUName = '%s' "
				+ "ORDER BY `Messages`.`Timestamp` ASC"
				, senderUname, receiverUname, senderUname, receiverUname);
		try {
			Statement s = sqlConnection.createStatement();
			ResultSet res = s.executeQuery(query);
			
			int count = 0;
			
			while (res.next()) {
				
				String sender = res.getString("SenderUname");
				String receiver = res.getString("ReceiverUName");
				String msg = res.getString("Message");
				
				//System.out.printf("Sender: %s, Receiver: %s, Message: %s\n", sender, receiver, msg);
				result = true;
				
				count++;
			}
			
			//System.out.println(count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(true, result);
	}
	
	
	@Test 
	void getMessagesTest(){
		
		Connection sqlConnection = Server.establishSqlConnection();
		
		//Pull all the relevant messages from the server
		String senderUname = "Aliceon";
		String receiverUname = "RemyMouse";

		String query = String.format("SELECT * FROM `Messages` WHERE SenderUName = '%s' AND ReceiverUName = '%s' OR SenderUName = '%s' AND ReceiverUName = '%s' ORDER BY `Messages`.`Timestamp` ASC;"
				, senderUname, receiverUname, receiverUname, senderUname);
		
		try {
			Statement s = sqlConnection.createStatement();
			ResultSet res = s.executeQuery(query);
			
			//Convert the table data into chat message objects and add them into an arraylist
			ArrayList<ChatMessage> allMessages = new ArrayList<ChatMessage>();
			
			int count = 0;
			
			while (res.next()) {
				
				String sender = res.getString("SenderUName");
				String receiver = res.getString("ReceiverUname");
				String message = res.getString("Message");
				int timeStamp = res.getInt("Timestamp");
				
				
				count++;
				//allMessages.add(new ChatMessage(sender, receiver, message, timeStamp)); 
			}
			System.out.println("getMessagesTest count: " + count);

		}
		catch (Exception e) {
			e.printStackTrace();
		}	
	}
		
}
