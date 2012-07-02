package com.wikispaces.lsfn.Environment;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.*;
import com.wikispaces.lsfn.Shared.LSFN.*;
import java.io.*;
import java.util.*;

public class EnvironmentServer implements Runnable {
    private ClientHandler SHIP_server;
    private Thread SHIP_server_thread;
    private boolean running;
    private BufferedReader stdin;
    
    private Space space;
    
    EnvironmentServer() {
        SHIP_server = null;
        SHIP_server_thread = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
        
        space = new Space(1000, 1000);
    }
    
    public void run() {
        start_SHIP_server();
        
        running = true;
        while(running) {
            // Process user input;
            process_stdin();
            
            // Process messages from SHIPs
            process_SHIP();
            
            // Run the tick() functions of everything in space
            space.tick();
            
            // Send back state output
            send_position_output();
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO
                e.printStackTrace();
            }
        }
        
        stop_SHIP_server();
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
    
    private void process_SHIP() {
        if(SHIP_server != null) {
            // Handle new connections
            Integer[] SHIP_IDs = SHIP_server.get_new_connections();
            for(int i = 0; i < SHIP_IDs.length; i++) {
                ES handshake = ES.newBuilder()
                        .setHandshake(ES.Handshake.newBuilder()
                                .setType(ES.Handshake.Type.HELLO)
                                .setShipID(SHIP_IDs[i])
                                .build())
                        .build();
                SHIP_server.send(SHIP_IDs[i], handshake.toByteArray());
            }
            
            // Handle existing connections
            HashMap<Integer, byte[][]> messages = SHIP_server.read_all();
            Iterator<Integer> message_iterator = messages.keySet().iterator();
            while(message_iterator.hasNext()) {
                Integer message_ID = message_iterator.next();
                byte[][] message_array = messages.get(message_ID);
                for(int i = 0; i < message_array.length; i++) {
                    process_SHIP_message(message_ID, message_array[i]);
                }
            }
        }
    }
    
    private void process_SHIP_message(Integer SHIP_ID, byte[] message) {
        SE parsed_message = null;
        try {
            parsed_message = SE.parseFrom(message);
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
                        SHIP_server.remove_socket(SHIP_ID);
                        break;
                }
            }
            
            if(parsed_message.hasMovement()) {
                Ship.data_from_SHIPs(SHIP_ID, parsed_message.getMovement());
            }
        }
    }
    
    private void start_SHIP_server() {
        try {
            SHIP_server = new ClientHandler(14613);
        } catch(IOException e) {
            SHIP_server = null;
        }
        
        // Check if we have successfully created a ClientHandler to serve as a host for SHIPs
        if(SHIP_server != null) {
            SHIP_server_thread = new Thread(SHIP_server);
            SHIP_server_thread.start();
        }
    }
    
    private void stop_SHIP_server() {
        if(SHIP_server != null) {
            // Send a goodbye to each connected client
            ES goodbye_message = ES.newBuilder()
                    .setHandshake(ES.Handshake.newBuilder()
                            .setType(ES.Handshake.Type.GOODBYE)
                            .build())
                    .build();
            SHIP_server.send_to_all(goodbye_message.toByteArray());
            
            // Close the server
            try {
                SHIP_server.close();
                SHIP_server_thread.join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            SHIP_server = null;
            SHIP_server_thread = null;
        }
    }
    
    private void send_position_output() {
        ES state_output = ES.newBuilder()
                .setPositions(Ship.get_proto_positions())
                .build();
        SHIP_server.send_to_all(state_output.toByteArray());
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnvironmentServer().run();
    }
}
