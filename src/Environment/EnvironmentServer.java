package com.wikispaces.lsfn.Environment;

import com.wikispaces.lsfn.Shared.*;
import java.io.*;
import java.util.*;

public class EnvironmentServer implements Runnable {

    public void run() {
        ClientHandler SHIP_server = null;
        try {
            SHIP_server = new ClientHandler(14613);
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
        
        // Check if the ClientHandler opened successfully.
        if(SHIP_server != null) {
            Thread client_thread = new Thread(SHIP_server);
            client_thread.start();
            
            boolean running = true;
            while(running) {
                HashMap<Integer, String[]> messages = SHIP_server.read_all();
                Iterator<Integer> message_iterator = messages.keySet().iterator();
                while(message_iterator.hasNext()) {
                    Integer message_ID = message_iterator.next();
                    String[] message_array = messages.get(message_ID);
                    for(int i = 0; i < message_array.length; i++) {
                        System.out.println("Socket " + message_ID + " sent: \"" + message_array[i] + "\" length " + message_array[i].length());
                        SHIP_server.send(message_ID, message_array[i]);
                        if(message_array[i].equals("Stop server.")) {
                            running = false;
                        } else if(message_array[i].equals("Bye.")) {
                            SHIP_server.remove_socket(message_ID);
                            break;
                        }
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
            SHIP_server.send_to_all("Server shutting down.");
            
            // Tries to close and join the SHIP_server.
            try {
                SHIP_server.close();
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
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnvironmentServer().run();
    }
}
