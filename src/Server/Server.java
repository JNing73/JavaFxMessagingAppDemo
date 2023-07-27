package Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import Client.User;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import serverUtilities.ServerConsole;



public class Server {
	
	ServerSocket listener;
	private TextArea statusConsole;
	private ObservableList<User> connectedUsers; //A common list that all serverthreads will be able to modify
	private HashMap<String,Socket> connectedUsersMap;
	final Connection sqlConnection;
	
		
	/**
	 * Starts the server
	 * @param connectedUsers 
	 * @param statusConsole 
	 * 
	 * @param port - in order to function correctly, should NOT be an occupied port
	 * @throws Exception 
	 */
	public Server(ObservableList<User> connectedUsers, TextArea statusConsole, int port) throws Exception {
		
		//This defines the port which the server is going operate / listen to when operating
		this.listener = new ServerSocket(port);
		
		//These are other objects it will need for its function
		this.statusConsole = statusConsole;
		this.connectedUsers = connectedUsers;
		this.sqlConnection = establishSqlConnection(); //start a new sql connection
		this.connectedUsersMap = new HashMap<String, Socket>();
		
		ServerConsole.message(statusConsole, "Threaded Server running on " + port);
	}
	
	
	
	
	/**
	 * 
	 * Opens up the server for incoming client connections. Multithreaded, so each connection
	 * will be handled by an individual server thread
	 */
	public void run() throws Exception {
		
		System.out.println("I'm Running Now");
		
		//Server will continuously run, and pass on the responsibility of the task to the thread
		try {
			while (true) {
				//Listen for connections from a client
				Socket clientSocket = listener.accept();
				
				//Pass that client connection onto a new thread
				new ServerThread(sqlConnection, connectedUsers, connectedUsersMap, statusConsole, clientSocket).start();
			}
		}
		finally {
			//close the listener when done, not sure if this code is reachable?
			listener.close(); 
			System.out.println("Listener Closed");
		}
	}




	/**
	 * Stops the server from accepting connections
	 */
	public void stop() {
		
		try {
			ServerConsole.message(statusConsole, "Server Stopped");
			listener.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}




	public static Connection establishSqlConnection() {
		
		try {

			//Localhost connection
	        String databaseUser = "validUserName"; //Enter your username here
	        String databaseUserPass = "validPassword";//Enter your password here
	        
	        Class.forName("com.mysql.jdbc.Driver");
	        Connection connection = null;
	        String url = "jdbc:mysql://localhost/demoMsgDatabase";
	        connection = DriverManager.getConnection(url, databaseUser, databaseUserPass);
	        
	        
	        return connection;

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		
		
		return null;
	}
	
	

}