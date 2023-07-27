package common;

import java.io.Serializable;


/**
 * Object that represents a user-user message
 *
 */
public class ChatMessage implements Serializable {

	private static final long serialVersionUID = -8808647869600393557L;
	
	private String sender;
	private String receiver;
	private String message;
	private String timeStamp;
	
	public ChatMessage(String sender, String receiver, String message, String timeStamp) {
		
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.timeStamp = timeStamp;
	}

	public String getSender() {
		return sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getMessage() {
		return message;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	//Formats the message depending on whether it was written by the sender or the receiver
	public String chatFormat(String user) {
		
		String sendingPerson = "     " + this.sender + " said: ";
		timeStamp = "     (" + timeStamp + ")";
		
		if (user.equals(this.sender)) {
			
			sendingPerson = "You said: ";
			timeStamp = timeStamp.strip();
		}
		
		return sendingPerson + this.message + "\n" + timeStamp + "\n";
	}
	
	
	
}
