package serverUtilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.TextArea;

public class ServerConsole {

	//Updates a Textarea with the given text and also adds in a timestamp
	public static void message(TextArea display, String message) {
		
		   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YY HH:mm:ss  ");  
		   LocalDateTime now = LocalDateTime.now();  
		   display.appendText(dtf.format(now));
		   
		   display.appendText(message + "\n");
		   
	}
}
