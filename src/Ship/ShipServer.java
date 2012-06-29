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
        start_INT_server();
        
        running = true;
        while(running) {
            
            // Collect input from stdin
            process_stdin();
            
            // Get all messages fron connected INTs
            process_INTs();
            
            // Get messages from the ENV (if it's connected)
            process_ENV();
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        stop_ENV_client(true);
        stop_INT_server();
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
        if(INT_server != null) {
            // Handle new connections
            Integer[] INT_IDs = INT_server.get_new_connections();
            for(int i = 0; i < INT_IDs.length; i++) {
                LSFN.SI handshake = LSFN.SI.newBuilder()
                        .setHandshake(LSFN.SI.Handshake.newBuilder()
                                .setType(LSFN.SI.Handshake.Type.HELLO)
                                .setPlayerID(INT_IDs[i])
                                .build())
                        .build();
                INT_server.send(INT_IDs[i], handshake.toByteArray());
            }
            
            // Handle existing connections
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
            
            // Handle new disconnections
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
            if(parsed_message.hasHandshake()) {
                switch(parsed_message.getHandshake()) {
                    case HELLO:
                        // Obsolete
                        break;
                    case GOODBYE:
                        INT_server.remove_socket(INT_ID);
                        break;
                }
            }
            
            if(parsed_message.hasCommand()) {
                LSFN.IS.SHIP_ENV_command command = parsed_message.getCommand();
                switch(command.getType()) {
                    case CONNECT:
                        start_ENV_client(command.getHost(), command.getPort());
                        break;
                    case DISCONNECT:
                        stop_ENV_client(true);
                        break;
                    case RECONNECT:
                        stop_ENV_client(true);
                        start_ENV_client(command.getHost(), command.getPort());
                        break;
                }
            }
        }
    }
    
    private void start_INT_server() {
        try {
            INT_server = new ClientHandler();
        } catch(IOException e) {
            INT_server = null;
        }
        
        // Check if we have successfully created a ClientHandler to serve as a host for INTs
        if(INT_server != null) {
            INT_server_thread = new Thread(INT_server);
            INT_server_thread.start();
        }
    }
    
    private void stop_INT_server() {
        if(INT_server != null) {
            try {
                INT_server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Send a goodbye to each connected client
            LSFN.SI goodbye_message = LSFN.SI.newBuilder()
                    .setHandshake(LSFN.SI.Handshake.newBuilder()
                            .setType(LSFN.SI.Handshake.Type.GOODBYE)
                            .build())
                    .build();
            INT_server.send_to_all(goodbye_message.toByteArray());
            
            // Close the server
            try {
                INT_server_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            INT_server = null;
            INT_server_thread = null;
        }
    }
    
    private void process_ENV() {
        if(ENV_client != null) {
            byte[][] messages = ENV_client.get_messages();
            for(int i = 0; i < messages.length; i++) {
                process_ENV_message(messages[i]);
            }
        }
    }
    
    private void process_ENV_message(byte[] message) {
        LSFN.ES parsed_message = null;
        try {
            parsed_message = LSFN.ES.parseFrom(message);
            System.out.print(parsed_message.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        
        if(parsed_message != null) {
            if(parsed_message.hasHandshake()) {
                LSFN.SI return_message = null;
                switch(parsed_message.getHandshake().getType()) {
                    case HELLO:
                        return_message = LSFN.SI.newBuilder()
                                .setStatus(LSFN.SI.SHIP_ENV_status.newBuilder()
                                        .setState(LSFN.SI.SHIP_ENV_status.State.CONNECTED)
                                        .setShipID(parsed_message.getHandshake().getShipID())
                                        .build())
                                .build();
                        break;
                    case GOODBYE:
                        return_message = LSFN.SI.newBuilder()
                                .setStatus(LSFN.SI.SHIP_ENV_status.newBuilder()
                                        .setState(LSFN.SI.SHIP_ENV_status.State.DISCONNECTED)
                                        .build())
                                .build();
                        stop_ENV_client(false);
                        break;
                }
                INT_server.send_to_all(return_message.toByteArray());
            }
        }
    }
    
    private void start_ENV_client(String host, int port) {
        try {
            ENV_client = new Listener(host, port);
        } catch (IOException e) {
            ENV_client = null;
        }
        
        if(ENV_client != null) {
            ENV_client_thread = new Thread(ENV_client);
            ENV_client_thread.start();
        }
    }
    
    private void stop_ENV_client(boolean send_goodbye) {
        if(ENV_client != null) {
            try {
                ENV_client_thread.interrupt();
                ENV_client_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if(send_goodbye) {
                LSFN.SE handshake = LSFN.SE.newBuilder().setHandshake(LSFN.SE.Handshake.GOODBYE).build();
                ENV_client.send(handshake.toByteArray());
            }
            
            try {
                ENV_client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            ENV_client = null;
            ENV_client_thread = null;
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
