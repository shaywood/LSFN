package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.*;
import com.google.protobuf.*;
import java.io.*;
import java.util.*;

public class ShipServer implements Runnable {

    private ClientHandler INT_server;
    private Thread INT_server_thread;
    private Listener ENV_client;
    private Thread ENV_client_thread;
    private boolean running;
    
    ShipServer() {
        INT_server = null;
        INT_server_thread = null;
        ENV_client = null;
        ENV_client_thread = null;
    }
    
    /**
     * Treat this as the SHIP program's main entry point.
     */
    public void run() {
        try {
            INT_server = new ClientHandler();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        // Check if we have successfully created a ClientHandler to serve as a host for INTs
        if(INT_server != null) {
            INT_server_thread = new Thread(INT_server);
            INT_server_thread.start();
            
            // Now start processing INT input
            running = true;
            while(running) {
                // Get all messages fron connected INTs
                HashMap<Integer, byte[][]> all_messages = INT_server.read_all();
                Iterator<Integer> INT_ID_iterator = all_messages.keySet().iterator();
                while(INT_ID_iterator.hasNext()) {
                    Integer INT_ID = INT_ID_iterator.next();
                    byte[][] messages = all_messages.get(INT_ID);
                    if(messages == null) continue;
                    for(int i = 0; i < messages.length; i++) {
                        process_INT_message(INT_ID, messages[i]);
                    }
                }
                
                /*
                if(ENV_client != null) {
                    String[] messages = ENV_client.get_messages();
                    for(int i = 0; i < messages.length; i++) {
                        process_ENV_message(messages[i]);
                    }
                }*/
                
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            /*
            if(ENV_client != null) {
                ENV_client.send("Bye.");
                stop_ENV_client();
            }*/
            
            stop_INT_server();
        }
    }
    
    private void process_INT_message(Integer INT_ID, byte[] message) {
        String byte_str = "";
        for(int i = 0; i < message.length; i++) {
            String hex_pair = Integer.toHexString(message[i]);
            if(hex_pair.length() == 1) hex_pair = "0" + hex_pair;
            byte_str += hex_pair;
        }
        System.out.println("message bytes: " + byte_str);
        try {
            LSFN.IS parsed_message = LSFN.IS.parseFrom(message);
            System.out.println(parsed_message.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        
        /*
        if(message.equals("Connect to ENV.")) {
            if(ENV_client == null) {
                // Start the ENV_client.
                if(start_ENV_client()) {
                    // Tell the client the good news.
                    INT_server.send(INT_ID, "Connected to ENV successfully.");
                } else {
                    // Tell the client the bad news.
                    INT_server.send(INT_ID, "Failed to connect to ENV.");
                }
            } else {
                // Tell the INT that the SHIP is not connected to ENV.
                INT_server.send(INT_ID, "Cannot send message to ENV, not connected.");
            }
        } else if (message.equals("Disconnect from ENV.")) {
            if(ENV_client != null) {
                ENV_client.send("Bye.");
                stop_ENV_client();
                INT_server.send(INT_ID, "Disconnected from ENV.");
            } else {
                // Tell the INT that the SHIP is not connected to ENV.
                INT_server.send(INT_ID, "Cannot send message to ENV, not connected.");
            }
        } else if(message.equals("Stop ENV.")) {
            if(ENV_client != null) {
                ENV_client.send("Stop server.");
            } else {
                // Tell the INT that the SHIP is not connected to ENV.
                INT_server.send(INT_ID, "Cannot send message to ENV, not connected.");
            }
        } else if(message.equals("Stop server.")) {
            running = false;
            INT_server.send_to_all("Server shutting down.");
        } else if(message.startsWith("Tell ENV ") && message.length() > 9) {
            if(ENV_client != null) {
                // Send a message to the ENV.
                ENV_client.send("Client " + INT_ID + " says \"" + message.substring(9) + "\"");
            } else {
                // Tell the INT that the SHIP is not connected to ENV.
                INT_server.send(INT_ID, "Cannot send message to ENV, not connected.");
            }
        } else {
            // Echo the message.
            INT_server.send(INT_ID, message);
        }*/
    }
    
    /*
    private void process_ENV_message(String message) {
        if(message.equals("Server shutting down.")) {
            stop_ENV_client();
            INT_server.send_to_all("ENV has shutdown");
        } else {
            INT_server.send_to_all("ENV says \"" + message + "\"");
        }
    }
    
    private boolean start_ENV_client() {
        try {
            ENV_client = new Listener("localhost", 14613);
        } catch (IOException e) {
            ENV_client = null;
            return false;
        }
        ENV_client_thread = new Thread(ENV_client);
        ENV_client_thread.start();
        return true;
    }
    
    private void stop_ENV_client() {
        try {
            ENV_client_thread.interrupt();
            ENV_client_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ENV_client = null;
        ENV_client_thread = null;
    }*/
    
    private void stop_INT_server() {
        try {
            INT_server.close();
            INT_server_thread.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Starts a SHIP.
     * @param args
     */
    public static void main(String[] args) {
        new ShipServer().run();
    }
    
}
