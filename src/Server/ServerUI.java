package Server;
	
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Client.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import serverUtilities.ServerConsole;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public final class ServerUI extends Application {
	
	Server server;
	
	Button startBtn;
	Button stopBtn;
	TextField portInput;
	final TextArea statusConsole = new TextArea("");
	
	private TableView<User> connectedUsersTable = new TableView<User>();
	final ObservableList<User> connectedUsers = FXCollections.observableArrayList();

	private Text statusIndicator;
	
	
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			
			
			//Stage setup and launch
			Scene connectScene = connectScene();
			//loginScene.getStylesheets().add(getClass().getResource("../common/application.css").toExternalForm());
			primaryStage.setTitle("Messaging Server");
			primaryStage.setScene(connectScene);
			primaryStage.show();
			
			primaryStage.setOnCloseRequest(e->{
				if (server != null) {
					server.stop();
					server = null;
				}
				Platform.exit();
			});
			
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	


	
	/**
	 * Starts a threaded server on the selected port number on an independent service.
	 */
	private void startBtnFunctionality() {
		
		startBtn.setOnAction(e -> {
			
			int portNum = Integer.parseInt(portInput.getText()); //user selected port number
			
			
			try {
				
				if (server == null) {
					this.server = new Server(connectedUsers, statusConsole, portNum); //Initialise the server
					ServerService service = new ServerService(this.server); //Place it into a service
					
					//Start the service which also starts the listener on the server. Service acts as an in-between
					//To stop the endless loop of the server from freezing the GUI
					service.start(); 
					
					statusIndicator.setText("Running");
					statusIndicator.setFill(Color.GREEN);
					
				}
				else {
					//System.out.println("Server already running, stop the current one first");
					ServerConsole.message(statusConsole, "Server already running, stop the current one first");
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}
	
	
	/**
	 * Stops listening into the server
	 */
	private void stopBtnFunctionality() {
		
		
		stopBtn.setOnAction(e -> {
			
			if (server != null) {
				server.stop();
				server = null;
				
				statusIndicator.setText("Not Running");
				statusIndicator.setFill(Color.RED);
			}
			else {
				System.out.println("No server active");
				ServerConsole.message(statusConsole, "No server active");

			}
		});
	}




	/**
	 * Creates a VBOX with a layout for user input and buttons
	 * then adds the listeners to them
	 * @return
	 */
	private Scene connectScene() {
		
		VBox UI = new VBox();
		
		Text serverIP = new Text();
		
		String publicIP = "Could not determine public IP address right now";
		
		try { 
		String urlString = "http://checkip.amazonaws.com/";
		URL url = new URL(urlString);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		publicIP = br.readLine();
		serverIP.setText(publicIP);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//Start Server button
		startBtn = new Button("Start Server");
		//Stop Server button
		stopBtn = new Button("Stop Server");
		
		statusIndicator = new Text("Not Running");
		statusIndicator.setFill(Color.RED);
		
		HBox controls = new HBox();
		controls.getChildren().addAll(startBtn, stopBtn, statusIndicator);
		
		//Port Input
		Text inputPrompt = new Text("Select a port number");
		portInput = new TextField("9090");
		
		//Status Console
		Label statusLabel = new Label("Status Console:");
		statusConsole.setWrapText(true);
		statusConsole.setEditable(false);
		
		//The Table
		Label loggedInUsers = new Label("Connected Users");		
		buildConnectedUserTable();
		
				
		//Populate the pane
		UI.getChildren().addAll(serverIP, inputPrompt, portInput, controls, statusLabel, statusConsole, 
				loggedInUsers, connectedUsersTable);
		
		makeServerGUIFunctional();
		
		return new Scene(UI,800,600);
	}
	
	
	/**
	 * Attach the relevant listeners to the UI
	 */
	private void makeServerGUIFunctional() {
		
		startBtnFunctionality();
		stopBtnFunctionality();
				
	}
	
	
	//Table syntax modelled off Ali's example code
	@SuppressWarnings("unchecked")
	private void buildConnectedUserTable() {
		
		connectedUsersTable.setEditable(true);
		
		TableColumn<User,String> usernameCol = new TableColumn<User,String>("Username");
		usernameCol.setMinWidth(100);
		usernameCol.setCellValueFactory(
				new PropertyValueFactory<User, String>("username"));
		
		
		TableColumn<User,String> ipColumn = new TableColumn<User,String>("IP Address");
		ipColumn.setMinWidth(200);
		ipColumn.setCellValueFactory(
				new PropertyValueFactory<User, String>("ip"));
		
		TableColumn<User, Integer> portColumn = new TableColumn<User,Integer>("Client Port");
		portColumn.setMinWidth(200);
		portColumn.setCellValueFactory(
				new PropertyValueFactory<User, Integer>("portNum"));
		
		TableColumn<User,String> loggedInColumn = new TableColumn<User,String>("Login Status");
		loggedInColumn.setMinWidth(200);
		loggedInColumn.setCellValueFactory(
				new PropertyValueFactory<User, String>("loggedIn"));
		
		connectedUsersTable.setItems(connectedUsers);
		connectedUsersTable.getColumns().addAll(ipColumn, portColumn, loggedInColumn, usernameCol);
	}





	public static void main(String[] args) {
		launch(args);
	}
}
