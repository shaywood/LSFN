package com.wikispaces.lsfn.Interface;

import com.google.protobuf.*;
import com.wikispaces.lsfn.Interface.Display2D.MapDisplay;
import com.wikispaces.lsfn.Shared.*;
import java.io.*;

public class InterfaceClient {
    private Listener SHIP_client;
    private Thread SHIP_client_thread;
    private boolean running;
    private BufferedReader stdin;
    
    InterfaceClient() {
        SHIP_client = null;
        SHIP_client_thread = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
        new MapDisplay();
    }
    
    public void run() {        
        running = true;
        while(running) {
            // First we try to read some input from stdin if there is any.
            process_stdin();
            
            // Then we get any messages the server has sent to us, if any. TODO
            process_SHIP();
            
            // Lastly, if we haven't told the program to stop, we sleep for 1/50 seconds (20ms)
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // When we shut down, we close the SHIP_client and join the thread.
        System.out.println("Shutting down.");
        stop_SHIP_client(true);
        System.exit(0);
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
        String[] parts = message.split(" ");
        int num_parts = parts.length;
        
        if(num_parts >= 1 && parts[0].equals("stop")) {
            running = false;
        } else if(num_parts >= 1 && parts[0].equals("connect")) {
            if(num_parts == 4 && parts[1].equals("remote")) {
                LSFN.IS sendable = LSFN.IS.newBuilder()
                        .setCommand(LSFN.IS.SHIP_ENV_command.newBuilder()
                                .setType(LSFN.IS.SHIP_ENV_command.Type.CONNECT)
                                .setHost(parts[2])
                                .setPort(Integer.parseInt(parts[3]))
                                .build())
                        .build();
                SHIP_client.send(sendable.toByteArray());
            } else if(num_parts == 3) {
                start_SHIP_client(parts[1], Integer.parseInt(parts[2]));
            }
        } else if(num_parts >= 1 && parts[0].equals("disconnect")) {
            if(num_parts == 2 && parts[1].equals("remote")) {
                LSFN.IS sendable = LSFN.IS.newBuilder()
                        .setCommand(LSFN.IS.SHIP_ENV_command.newBuilder()
                                .setType(LSFN.IS.SHIP_ENV_command.Type.DISCONNECT)
                                .build())
                        .build();
                SHIP_client.send(sendable.toByteArray());
            } else if(num_parts == 1) {
                stop_SHIP_client(true);
            }
        }
    }
    
    private void process_SHIP() {
        if(SHIP_client != null) {
            byte[][] messages = SHIP_client.get_messages();
            for(int i = 0; i < messages.length; i++) {
                process_SHIP_message(messages[i]);
            }
        }
    }
        
    private void process_SHIP_message(byte[] message) {
        try {
            LSFN.SI parsed_message = LSFN.SI.parseFrom(message);
            System.out.print(parsed_message.toString());
            if(parsed_message.hasHandshake() && parsed_message.getHandshake().getType() == LSFN.SI.Handshake.Type.GOODBYE) {
                stop_SHIP_client(false);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
    
    private void start_SHIP_client(String host, int port) {
        try {
            SHIP_client = new Listener(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the SHIP.");
        }
        
        // Check if socket creation succeeded
        if(SHIP_client != null) {
            SHIP_client_thread = new Thread(SHIP_client);
            SHIP_client_thread.start();
        }
    }
    
    private void stop_SHIP_client(boolean send_goodbye) {
        if(SHIP_client != null) {
            try {
                SHIP_client_thread.interrupt();
                SHIP_client_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            if(send_goodbye) {
                LSFN.IS handshake = LSFN.IS.newBuilder().setHandshake(LSFN.IS.Handshake.GOODBYE).build();
                SHIP_client.send(handshake.toByteArray());
            }
            
            try {
                SHIP_client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            SHIP_client = null;
            SHIP_client_thread = null;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new InterfaceClient().run();
    }

}
