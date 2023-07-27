package Client;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import common.Commands;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class ClientUI extends Application {
	
	private ClientApp clientApp;
	final Text activeUsername = new Text("");
	final ObservableList<String> allUsers = FXCollections.observableArrayList();

	private Stage UIstage;
		
	Scene connection;
	private TextField ipInput;
	private TextField portInput;
	private Button connectToServer;
	
	Scene login;
	private TextField usernameInput;
	private PasswordField passwordInput;
	private Button loginBtn;
	private Button disconnect;
	private Button registerBtn;
	
	private Scene register;
	private TextField newUsernameInput;
	private PasswordField newPasswordInput;
	private PasswordField newPasswordConfirmInput;
	private Button completeRegistration;
	private Button backToLogin;
	private TextField fnameInput;
	private TextField lnameInput;
	
	Scene userActivity;
	private ComboBox<String> interactableUsers;
	private Text otherUserOnlineStatus;
	final TextArea messagesDisplay = new TextArea();
	private Button logout;
	private ClientThread messageUpdater;
	private Label messageEntryLabel;
	private TextField messageEntryInput;
	private Button sendMsgBtn;
	private Button prodBtn;
	private String selectUserPrompt = "Select a user to interact with";
;
	

	
	
	//Initialise the UI with the connection screen
	@Override
	public void start(Stage primaryStage) {
		try {
			
			buildScenes(); //We setup the scene structure
			
			//scene.getStylesheets().add(getClass().getResource("../application/application.css").toExternalForm());
			primaryStage.setScene(connection);
			UIstage = primaryStage;
			primaryStage.show();
			primaryStage.setTitle("Messaging Client");
			
			//If the client UI is closed make sure the server knows that the user has left
			primaryStage.setOnCloseRequest(e-> {
				
				//Disconnect the client and make the server aware
				if (clientApp != null) {
					clientApp.stopExistingThread();
					clientApp.logout();
					clientApp.disconnectRequest();
				}
				
				Platform.exit();
			});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prepares all possible scenes the ClientUI will require and gives the appropriate sections functionality
	 */
	private void buildScenes() {
		
		this.connection = connectionScreen();
		this.login = loginScreen();
		this.userActivity = userActivityScreen();
		this.register = registerScreen();
		
		makeFunctional();
	}

	/**
	 * Creates a client connect-to-server screen
	 * @return
	 */
	private Scene connectionScreen() {
		
		VBox connectionUI = new VBox();
		connectionUI.setSpacing(5);
		
		//IP Entry
		Text labelForIP = new Text("Enter IP address");
		ipInput = new TextField();
		ipInput.setText("localhost");
		
		//Port number entry
		Text labelForPortInput = new Text("Enter Port Number");
		portInput = new TextField();
		portInput.setText("9090");
		
		
		//Connection Button
		connectToServer = new Button("Connect");
		
		
		connectionUI.getChildren().addAll(labelForIP, ipInput, labelForPortInput, portInput, connectToServer);
		
				
		return new Scene(connectionUI,800,600);
	}

	
	/**
	 * Produces a login screen
	 * @return
	 */
	private Scene loginScreen() {
		
		VBox UI = new VBox();
		
		Label username = new Label("Enter Username");
		usernameInput = new TextField();
		
		Label password = new Label("Enter Password");
		passwordInput = new PasswordField();
		
		loginBtn = new Button("Login");
		loginBtn.setMinWidth(70);
		
		registerBtn = new Button("Register");
		registerBtn.setMinWidth(70);

		disconnect = new Button("Disconnect");
		disconnect.setMinWidth(70);
				
		UI.getChildren().addAll(username, usernameInput, password, passwordInput, loginBtn, registerBtn, new Text(""), disconnect);
		
		//makeFunctional();
		
		
		return new Scene(UI , 800, 600);
	}
	
	
	//This is the scene for user to communicate with other users
	private Scene userActivityScreen() {
		
		VBox UI = new VBox();
				
		Text message = new Text("You are logged in as: ");
		//activeUsername from the global variable
		HBox userNameDisplay = new HBox();
		userNameDisplay.getChildren().addAll(message, activeUsername);
		
		
		interactableUsers = new ComboBox<String>(allUsers);
		otherUserOnlineStatus = new Text("");
		HBox otherUsers = new HBox();
		otherUsers.getChildren().addAll(interactableUsers, otherUserOnlineStatus);
		
		//User should not be able to edit anything in chatbox
		messagesDisplay.setEditable(false);
			
		messageEntryLabel = new Label("Type Message Below: ");
		messageEntryInput = new TextField();
		
		sendMsgBtn = new Button("Send");
		sendMsgBtn.setMinWidth(50);
		
		prodBtn = new Button("Prod them");
		prodBtn.setMinWidth(50);
		
		HBox interactionBtns = new HBox();
		interactionBtns.getChildren().addAll(sendMsgBtn, prodBtn);
		
		//We don't want these to be initially visible until a user selects a person to interact with
//		messageEntryInput.setVisible(false);
//		sendMsgBtn.setVisible(false);
//		messageEntryLabel.setVisible(false);
		
		logout = new Button("Logout");
		logout.setMinWidth(50);

		
		UI.getChildren().addAll(userNameDisplay, otherUsers, messagesDisplay, messageEntryLabel,
				messageEntryInput, interactionBtns, new Text(""), logout);
		
		return new Scene(UI, 800, 600);
	}
	
	//User registration screen
	private Scene registerScreen() {
		
		VBox UI = new VBox();
		
		Label newUsernameLabel = new Label("Enter a new Username");
		newUsernameInput = new TextField();
		
		Label fnameLabel = new Label("Your First Name:");
		fnameInput = new TextField();
		
		Label lnameLabel = new Label("Your Last Name: ");
		lnameInput = new TextField();
		
		Label newPasswordLabel = new Label("Select a Password");
		newPasswordInput = new PasswordField();
		
		Label confirmNewPasswordLabel = new Label("Re-enter Password");
		newPasswordConfirmInput = new PasswordField();
		
		completeRegistration = new Button("Register");
		completeRegistration.setMinWidth(100);
		
		backToLogin = new Button("Back to Login");
		backToLogin.setMinWidth(100);
		
		UI.getChildren().addAll(newUsernameLabel, newUsernameInput, fnameLabel, fnameInput, 
				lnameLabel, lnameInput, newPasswordLabel, newPasswordInput, confirmNewPasswordLabel, 
				newPasswordConfirmInput, completeRegistration, backToLogin);
		
		return new Scene(UI, 800, 600);
	}

	/**
	 * Method to quickly switch between scenes
	 * @param sceneName
	 */
	private void sceneSwitch(String sceneName) {
		
		if (sceneName.equals("connection")) {
			
			UIstage.setScene(connection);
		}
		else if (sceneName.equals("login")) {
			
			UIstage.setScene(login);
			messagesDisplay.clear(); //If someone logs out and in we need to make sure we clear that display 
			this.otherUserOnlineStatus.setText("");
		}
		else if (sceneName.equals("activity")) {
			
			UIstage.setScene(userActivity);
		}
		else if (sceneName.equals("register")) {
			
			UIstage.setScene(register);
		}
		
	}

	/**
	 * Groups all the methods which add functions to the buttons
	 */
	private void makeFunctional() {
		
		connectionButtonFunctionality();
		
		loginButtonFunctionality();
		disconnectButtonFunctionality();
		registerButtonFunctionality();

		completeRegistrationFunctionality();
		backToLoginFunctionality();
		
		
		logoutButtonFunctionality();
		
		//getAllusers can't be here because the clientApp object is still null at this point
		//So it wouldn't do anything
	}
	
	//The best time to call this is once we know who our user actually is ie. on login
	private void makeFunctionalPt2() {
		
		getAllUsers();
		viewConversationsFunctionality();
		sendMessagesFunctionality();
		
	}
	
	
	
	private void sendMessagesFunctionality() {
		
		sendMsgBtn.setOnAction(e->{
			
			String sender = activeUsername.getText();
			String receiver = interactableUsers.getSelectionModel().getSelectedItem();
			String messageToSend = messageEntryInput.getText();
			
		   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YY HH:mm:ss");  
		   LocalDateTime now = LocalDateTime.now(); 
		   String timestamp = dtf.format(now);
		   
		   	//user-proofing inputs
			if ((receiver.equals(this.selectUserPrompt) || receiver.equals(""))) {
				
				Alert noSelection = new Alert(AlertType.ERROR);
				noSelection.setContentText("You need to select a user first");
				noSelection.show();
			}
			else {
				
				clientApp.sendAmessage(sender, receiver, messageToSend, timestamp);
				//finally clear off the text to tidy up for the next message
				messageEntryInput.clear();
				
			} 
		});
		
	}

	/**
	 * Will populate the observable array list with all the users on the network, EXCEPT for yourself
	 */
	private void getAllUsers() {
		
		allUsers.clear(); //If the current user logs in and out, we don't want the combox box to keep getting bigger!
		
		ArrayList<String> userArrayList = clientApp.getAllUsers();
				
		for (String user : userArrayList) {
			allUsers.add(user);
		}
		
		allUsers.remove(activeUsername.getText());
		
	}
	
	
	/**
	 * Functionality for displaying conversations AND the prod button
	 */
	private void viewConversationsFunctionality() {
		
			interactableUsers.setValue("Select a user to interact with");

			interactableUsers.setOnAction(e->{
			
			
			//If switching between chats we want to cancel the thread before we make a new one
			if (messageUpdater != null) {
				messageUpdater.cancelThread();
				messageUpdater = null;
			}
			
			messagesDisplay.clear();
			
			//Get the other person that the client wants to interact with
			String otherUser = interactableUsers.getSelectionModel().getSelectedItem();
			
			clientApp.stopExistingThread();
			//Assign and start the relevant thread which will update the message display
			clientApp.startMessageThread(activeUsername.getText(), otherUser, messagesDisplay, otherUserOnlineStatus);
			
						
		});
		
		
		
		prodBtn.setOnAction(e->{
			
			String sender = activeUsername.getText();
			String receiver = interactableUsers.getSelectionModel().getSelectedItem();
			
			
			if (otherUserOnlineStatus.getText().equals(Commands.currentlyOfflineText)) {
				
				Alert offline = new Alert(AlertType.ERROR);
				offline.setContentText("That user is not online right now");
				offline.show();
				
			}
			else if ((receiver.equals(this.selectUserPrompt) || receiver.equals(""))) {
				
				Alert noSelection = new Alert(AlertType.ERROR);
				noSelection.setContentText("You need to select a user first");
				noSelection.show();
			}
			else {
				

				
				clientApp.prod(sender, receiver);

			}
			
			
		});
	}

	private void completeRegistrationFunctionality() {
		
		completeRegistration.setOnAction(e->{
			
			String newUsername = newUsernameInput.getText();
			String newPassword = newPasswordInput.getText();
			String confirmedNewPassword = newPasswordConfirmInput.getText();
			String fname = fnameInput.getText();
			String lname = lnameInput.getText();
			
			
			//General user proofing
			if (newPassword.equals("")) {
				
				Alert pwdRequired = new Alert(AlertType.ERROR);
				pwdRequired.setContentText("You must enter a password");
				pwdRequired.show();
			}
			//Passwords don't match
			else if (!(newPassword.equals(confirmedNewPassword))) {
				
				Alert confirmedPwdMismatch = new Alert(AlertType.ERROR);
				confirmedPwdMismatch.setContentText("You entered passwords do not match");
				confirmedPwdMismatch.show();
				
				newPasswordConfirmInput.setText("");
			}
			//No username entered
			else if (newUsername.equals("")) {
			
				Alert noUsrName = new Alert(AlertType.ERROR);
				noUsrName.setContentText("You must enter a username");
				noUsrName.show();
			}
			//All set to try to register
			else {
				//Make client send request
				boolean success = clientApp.registerRequest(newUsername, newPassword, fname, lname);
				
				//Inform success, redirect to login
				if (success) {
					
					Alert successAlert = new Alert(AlertType.INFORMATION);
					successAlert.setContentText("Registration Successful, Try Logging In");
					successAlert.showAndWait();
					
					sceneSwitch("login");
				}
				//I assume the only reason for failure is a duplicate user exists
				else {
					
					Alert duplicateName = new Alert(AlertType.ERROR);
					duplicateName.setContentText("There is already a user with this username, please choose another");
					duplicateName.show();
					
					newUsernameInput.clear();
				}
			}
		});
		
	}

	private void backToLoginFunctionality() {
		
		backToLogin.setOnAction(e->{
			
			sceneSwitch("login");
		});
		
	}

	private void registerButtonFunctionality() {
		
		registerBtn.setOnAction(e->{
			
			sceneSwitch("register");
		});
		
	}

	private void logoutButtonFunctionality() {
		
		logout.setOnAction(e->{
			
			//Tell the client logic to send a logout request
			clientApp.logout();
			
			
			//Just clear off any previous values
			activeUsername.setText("");
			usernameInput.clear();
			passwordInput.clear();
			
			sceneSwitch("login");
		});
		
	}

	private void loginButtonFunctionality() {
		
		
		loginBtn.setOnAction(e->{
			
			String uname = usernameInput.getText();
			String pwd = passwordInput.getText();
			
			boolean login = clientApp.login(uname, pwd);
			
			if (login) {
				activeUsername.setText(clientApp.getActiveUsername());
				sceneSwitch("activity");
				makeFunctionalPt2(); //Now that an appropriate clientApp has started we can add more functionality to the UI elements

				if (messageUpdater != null) {
					messageUpdater.cancelThread();
				}
			}
			else {
				
				Alert failedLogin = new Alert(AlertType.ERROR);
				failedLogin.setContentText("Incorrect Login Details, Please try again");
				failedLogin.show();
				
			}
		});
		
	}

	/**
	 * Fires up the client app with the given parameters and if handshake is successful
	 * then switches to the login screen.
	 */
	private void connectionButtonFunctionality() {
		
		connectToServer.setOnAction(e -> {
			
			String ipAddress = ipInput.getText();
			int portNum = Integer.parseInt(portInput.getText());
			clientApp = new ClientApp(ipAddress, portNum);
			
			if (clientApp.successfulHandshake) {
			
				sceneSwitch("login");
			}
			else {
				//Just to be safe, reset the variable if it didn't work
				clientApp = null;
			}
			
		});
	}
	
	/**
	 * Disconnect button closes the connection and switches back to the connection screen
	 */
	private void disconnectButtonFunctionality() {
		
		disconnect.setOnAction(e-> {
			clientApp.disconnectRequest();
			clientApp = null;
			sceneSwitch("connection");
		});
		
	}
	
	
	public static void main(String[] args) {
		
		
		launch(args);
	}
}