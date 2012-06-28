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
    private BufferedReader stdin;
    
    ShipServer() {
        INT_server = null;
        INT_server_thread = null;
        ENV_client = null;
        ENV_client_thread = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
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
            
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            
            // Now start processing INT input
            running = true;
            while(running) {
                // Collect input from stdin
                process_stdin();
                
                // Get all messages fron connected INTs
                process_INTs();
                
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
    
    private void process_stdin() {
        try {
            while(stdin.ready()) {
                process_stdin_message(stdin.readLine());
            }
        } catch (IOException e) {
            System.err.println("Failed to read from stdin.");
            e.printStackTrace();
            running = false;
        }
    }
    
    private void process_stdin_message(String message) {
        if(message.equals("stop")) running = false;
    }
    
    private void process_INTs() {
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
    }
    
    private void process_INT_message(Integer INT_ID, byte[] message) {
        LSFN.IS parsed_message = null;
        try {
            parsed_message = LSFN.IS.parseFrom(message);
            System.out.print(parsed_message.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        
        if(parsed_message != null) {
            LSFN.SI.Builder return_message_builder = LSFN.SI.newBuilder();
            
            if(parsed_message.hasHandshake()) {
                switch(parsed_message.getHandshake()) {
                    case HELLO:
                        return_message_builder
                                .setHandshake(LSFN.SI.Handshake.newBuilder()
                                        .setType(LSFN.SI.Handshake.Type.HELLO)
                                        .setPlayerID(INT_ID)
                                        .build());
                        break;
                    case GOODBYE:
                        return_message_builder
                            .setHandshake(LSFN.SI.Handshake.newBuilder()
                                    .setType(LSFN.SI.Handshake.Type.GOODBYE)
                                    .build());
                        break;
                }
            }
            
            LSFN.SI return_message = return_message_builder.build();
            INT_server.send(INT_ID, return_message.toByteArray());
            if(return_message.hasHandshake() && return_message.getHandshake().getType() == LSFN.SI.Handshake.Type.GOODBYE) {
                INT_server.remove_socket(INT_ID);
            }
        }
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
        // Send a goodbye to each connected client
        LSFN.SI goodbye_message = LSFN.SI.newBuilder()
                .setHandshake(LSFN.SI.Handshake.newBuilder()
                        .setType(LSFN.SI.Handshake.Type.GOODBYE)
                        .build())
                .build();
        INT_server.send_to_all(goodbye_message.toByteArray());
        
        // Close the server
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
