import java.io.*;
import java.util.*;


public class EnvironmentServer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClientHandler client_handler = null;
        try {
            client_handler = new ClientHandler();
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
        
        // Check if the ClientHandler opened successfully.
        if(client_handler != null) {
            Thread client_thread = new Thread(client_handler);
            client_thread.start();
            
            boolean running = true;
            while(running) {
                HashMap<Integer, String> messages = client_handler.read_all();
                Iterator<Integer> message_iterator = messages.keySet().iterator();
                while(message_iterator.hasNext()) {
                    Integer message_ID = message_iterator.next();
                    String message = messages.get(message_ID);
                    System.out.println("Socket " + message_ID + " sent: \"" + message + "\" length " + message.length());
                    client_handler.send(message_ID, message);
                    if(message.equals("Stop server.")) {
                        running = false;
                    } else if(message.equals("Bye.")) {
                        client_handler.remove_socket(message_ID);
                    }
                }
                
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // TODO
                    e.printStackTrace();
                }
            }
            
            // Tells the clients that the server is shutting down
            client_handler.send_to_all("Server shutting down.");
            
            // Tries to close and join the client_handler.
            try {
                client_handler.close();
                client_thread.join();
            } catch (IOException e) {
                // TODO If this exception occurs, nothing will come of it, the program is ending anyway.
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO If this exception occurs, nothing will come of it, the program is ending anyway.
                e.printStackTrace();
            }
        }
    }
}
