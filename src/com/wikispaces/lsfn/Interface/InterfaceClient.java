package com.wikispaces.lsfn.Interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;

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
        } else if(num_parts >= 1 && parts[0].equals("connect")) { // "connect remote" connects the ship to the environment server.
            if(num_parts == 4 && parts[1].equals("remote")) {
                if(network.isConnectedToSHIP()) {
                    IS sendable = IS.newBuilder()
                            .setCommand(IS.SHIP_ENV_command.newBuilder()
                                    .setType(IS.SHIP_ENV_command.Type.CONNECT)
                                    .setHost(parts[2])
                                    .setPort(Integer.parseInt(parts[3]))
                                    .build())
                            .build();
                    try {
                        network.sendToSHIP(sendable);
                    } catch (IOException e) {
                        System.err.println("Could not send connect command to remote.");
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Could not send: Not connected");
                }
            } else if(num_parts == 3) { // "connect" connects the interface to the ship. Port 14613 is default on the Ship server.
                try {
                    network.connectToSHIP(parts[1], Integer.parseInt(parts[2]));
                } catch (NumberFormatException e) {
                    System.err.println("\"" + parts[2] + "\" is not a valid integer.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("Could not connect to the server.");
                    e.printStackTrace();
                }
            }
        } else if(message.equals("disconnect remote")) { // "disconnect remote" disconnect the ship from the environment server.
            if(network.isConnectedToSHIP()) {
                IS sendable = IS.newBuilder()
                        .setCommand(IS.SHIP_ENV_command.newBuilder()
                                .setType(IS.SHIP_ENV_command.Type.DISCONNECT)
                                .build())
                        .build();
                try {
                    network.sendToSHIP(sendable);
                } catch (IOException e) {
                    System.err.println("Could not send disconnect command to remote.");
                    e.printStackTrace();
                }
            } else {
                System.err.println("Could not send: Not connected");
            }
        } else if(message.equals("disconnect")) {
            network.disconnectFromSHIP();
        } else {
        	System.out.println("Unknown message: " + message);
        }
        
    }
    
    private void process_network() {
        SI[] messages;
        try {
            messages = network.receiveFromSHIP();
            for(int i = 0; i < messages.length; i++) {
                process_SHIP_message(messages[i]);
            }
        } catch (IOException e) {
            System.err.println("Could not receive messages.");
            e.printStackTrace();
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
