package Client;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

/**
 * Concurrent service to initiate the clientthread
 *
 */
public class ClientService extends Service<String> {

	private ClientApp clientApp;
	private String user;
	private String otherUser;
	private TextArea messagesDisplay;
	private Text otherUserOnlineStatus;
	private ClientThread thread;


	public ClientService(ClientApp clientApp, String user, String otherUser, TextArea messagesDisplay, Text otherUserOnlineStatus) {
		
		this.clientApp = clientApp;
		this.user = user;
		this.otherUser = otherUser;
		this.messagesDisplay = messagesDisplay;
		this.otherUserOnlineStatus = otherUserOnlineStatus;

		
	}

	@Override
	protected Task<String> createTask() {

		return new Task<String>() {

			@Override
			protected String call() throws Exception {
				
				Platform.runLater(new Runnable() {


					@Override
					public void run() {
						
						thread = new ClientThread(clientApp, user, otherUser, messagesDisplay, otherUserOnlineStatus);
						thread.setDaemon(true);
						thread.start();
					}
				});
				
				
				return null;
			}
			
			
		};
	}
	
	public void stopService() {
		
		thread.cancelThread();
	}

}
