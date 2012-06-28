package com.wikispaces.lsfn.Interface;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.*;
import java.io.*;

public class InterfaceClient {
    private Listener SHIP_client;
    private Thread listen_thread;
    private boolean running;
    
    InterfaceClient() {
        SHIP_client = null;
        listen_thread = null;
    }
    
    public void run() {
        String host_str = "localhost";
        int port = 14612;
        try {
            SHIP_client = new Listener(host_str, port);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // Check if socket creation succeeded
        if(SHIP_client != null) {
            Thread listen_thread = new Thread(SHIP_client);
            listen_thread.start();
            
            // Perform Handshake with SHIP
            LSFN.IS handshake = LSFN.IS.newBuilder().setHandshake(LSFN.IS.Handshake.HELLO).build();
            SHIP_client.send(handshake.toByteArray());
            
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
        
            running = true;
            while(running) {
                userInput = "";
                // First we try to read some input from stdin if there is any.
                try {
                    if(stdIn.ready()) {
                        // If there is we send it to the server.
                        userInput = stdIn.readLine();
                        if(userInput.equals("stop")) {
                            handshake = LSFN.IS.newBuilder().setHandshake(LSFN.IS.Handshake.GOODBYE).build();
                            SHIP_client.send(handshake.toByteArray());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read from stdin.");
                    e.printStackTrace();
                    running = false;
                }
                
                // Then we get any messages the server has sent to us, if any. TODO
                byte[][] messages = SHIP_client.get_messages();
                for(int i = 0; i < messages.length; i++) {
                    process_SHIP_message(messages[i]);
                }
                
                // Lastly, if we haven't told the program to stop, we sleep for 1/50 seconds (20ms)
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            // When we shut down, we close the SHIP_client and join the thread.
            System.out.println("Shutting down.");
            try {
                listen_thread.interrupt();
                listen_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            try {
                SHIP_client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void process_SHIP_message(byte[] message) {
        try {
            LSFN.SI parsed_message = LSFN.SI.parseFrom(message);
            System.out.print(parsed_message.toString());
            if(parsed_message.hasHandshake() && parsed_message.getHandshake().getType() == LSFN.SI.Handshake.Type.GOODBYE) running = false;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new InterfaceClient().run();
    }

}
