package com.wikispaces.lsfn.Ship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;

public class ShipServer implements Runnable {
    private ShipNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    ShipServer() {
        network = new ShipNetworking(500);
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * Treat this as the SHIP program's main entry point.
     */
    public void run() {
        if(!network.openINTServer()) {
            System.out.println("Failed to open server");
        }
        
        running = true;
        while(running) {
        	processUserInput();

            network.handleConnectionUpdates();
            processMessagesFromExistingINTConnections();
            
            // Get messages from the ENV (if it's connected)
            processENV();
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.closeINTServer();
        network.disconnectFromENV();
    }

    private void processUserInput() {
        try {
            while(stdin.ready()) {
                processStdinMessage(stdin.readLine());
            }
        } catch (IOException e) {
            System.err.println("Failed to read from stdin.");
            e.printStackTrace();
            running = false;
        }
    }
    
    private void processStdinMessage(String message) {
        String[] parts = message.split(" ");
        int numParts = parts.length;
        
        if(message.equals("stop")) {
            running = false;
        } else if(parts[0].equals("rcon") && numParts >= 1) { // "connect remote" connects the ship to the environment server.
            if(network.isConnectedToENV() == ConnectionStatus.CONNECTED) {
                SE sendable = SE.newBuilder()
                        .setRcon(message.substring(5))
                        .build();
                if(network.sendToENV(sendable) != ConnectionStatus.CONNECTED) {
                    System.err.println("Sending failed.");
                }
            } else {
                System.err.println("Could not send message. Not connected");
            }
        } else if(parts[0].equals("connect") && numParts == 3) { // "connect" connects the interface to the ship. Port 14613 is default on the Ship server.
            try {
                if(network.connectToENV(parts[1], Integer.parseInt(parts[2])) == ConnectionStatus.CONNECTED) {
                    System.out.println("Connected to SHIP");
                } else {
                    System.err.println("Could not connect to SHIP");
                }
            } catch (NumberFormatException e) {
                System.err.println("\"" + parts[2] + "\" is not a valid integer.");
            }
        } else if(message.equals("disconnect")) {
            network.disconnectFromENV();
        } else {
            System.err.println("Unknown message: " + message);
        }
    }
    
	private void processMessagesFromExistingINTConnections() {
		HashMap<Integer, IS[]> allMessages = network.readAllFromINTs();
		for(Integer INTID : allMessages.keySet()) {
		    IS[] messages = allMessages.get(INTID);
		    if(messages == null) continue;
		    
		    for(int i = 0; i < messages.length; i++) {
		        processINTMessages(INTID, messages[i]);
		    }
		}
	}

    private void processINTMessages(Integer INTID, IS message) {        
        if(message != null) {
            if(message.hasRcon()) {
                processStdinMessage(message.getRcon());
            }
        }
    }

    private void processENV() {
        ES[] messages = network.receiveFromENV();
        if(messages != null) {
            for(int i = 0; i < messages.length; i++) {
                processENVMessage(messages[i]);
            }
        }
    }
    
    private void processENVMessage(ES message) {
        // TODO
    }
    
    /**
     * Starts a SHIP.
     * @param args
     */
    public static void main(String[] args) {
        new ShipServer().run();
    }
    
}
