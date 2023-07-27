package Server;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Creates a service that operates a continuously running threaded server
 * 
 */
public class ServerService extends Service<String> {
		
		Server ServicedServer;
		
		/**
		 * 
		 * @param ServicedServer - The threaded server that this service will be responsible for
		 */
		public ServerService(Server ServicedServer) {
			
			this.ServicedServer = ServicedServer;
			
			//If the task from createTask can be completed then this code block will run
			setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				//The source is the Task<String> returned by createTask()
				@Override
				public void handle(WorkerStateEvent e) {
					
					System.out.println(e.getSource().getValue()); //Prints "So I print out on success" as per the createTask() return value
				}
				
			});
		}

		
		/**
		 * What this service will do when run
		 */
		@Override
		protected Task<String> createTask() {
			
			return new Task<String>() {

				@Override
				protected String call() throws Exception {
					
					ServicedServer.run(); //This task runs 'forever' so the setOnSucceed code won't trigger
					//System.out.println("I Can Be Completed! So if I comment out ServicedServer.run(), ");
					return "I will print out on Success!";
				}
					
			};
		}
	}