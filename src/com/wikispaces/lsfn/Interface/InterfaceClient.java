package com.wikispaces.lsfn.Interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;
import com.wikispaces.lsfn.Interface.InterfaceNetworking;

public class InterfaceClient {
    private InterfaceNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    InterfaceClient() {
        network = new InterfaceNetworking();
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    int cycle_time_ms = 20;
    double cycle_time = ((double)cycle_time_ms)/1000.0;
	
 
    public void run() {      
        running = true;
        while(running) {
            process_console_input();
            process_network();
            
            try {
                Thread.sleep(cycle_time_ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // When we shut down, we close the SHIP_client and join the thread.
        System.out.println("Shutting down.");
        network.disconnectFromSHIP();
        System.exit(0);
    }

	private void process_console_input() {
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
        
        if(message.equals("stop")) {
            running = false;
        } else if(parts[0].equals("rcon") && num_parts >= 1) { // "connect remote" connects the ship to the environment server.
            if(network.isConnectedToSHIP() == ConnectionStatus.CONNECTED) {
                IS sendable = IS.newBuilder()
                        .setRcon(message.substring(5))
                        .build();
                if(network.sendToSHIP(sendable) != ConnectionStatus.CONNECTED) {
                    System.err.println("Sending failed.");
                }
            } else {
                System.err.println("Could not send message. Not connected");
            }
        } else if(parts[0].equals("connect") && num_parts == 3) { // "connect" connects the interface to the ship. Port 14613 is default on the Ship server.
            try {
                if(network.connectToSHIP(parts[1], Integer.parseInt(parts[2])) == ConnectionStatus.CONNECTED) {
                    System.out.println("Connected to SHIP");
                } else {
                    System.err.println("Could not connect to SHIP");
                }
            } catch (NumberFormatException e) {
                System.err.println("\"" + parts[2] + "\" is not a valid integer.");
            }
        } else if(message.equals("disconnect")) {
            network.disconnectFromSHIP();
        } else {
        	System.err.println("Unknown message: " + message);
        }
        
    }
    
    private void process_network() {
        if(network.isConnectedToSHIP() == ConnectionStatus.CONNECTED) {
            SI[] messages;
            messages = network.receiveFromSHIP();
            if(messages == null) {
                System.err.println("Could not receive messages.");
            } else {
                for(int i = 0; i < messages.length; i++) {
                    process_SHIP_message(messages[i]);
                }
            }
        }
    }
        
    private void process_SHIP_message(SI message) {
        System.out.print(message.toString());
        if(message.hasHandshake() && message.getHandshake().getType() == SI.Handshake.Type.GOODBYE) {
            network.disconnectFromSHIP();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new InterfaceClient().run();
    }

}
