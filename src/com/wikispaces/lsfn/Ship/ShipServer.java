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
        	processStdin();

            network.handleConnectionUpdates();
            processINTs();
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

    private void processStdin() {
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
                    System.out.println("Connected to ENV");
                } else {
                    System.err.println("Could not connect to ENV");
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
    
	private void processINTs() {
		HashMap<Integer, IS[]> allMessages = network.readAllFromINTs();
		for(Integer INTID : allMessages.keySet()) {
		    IS[] messages = allMessages.get(INTID);
		    if(messages == null) continue;
		    
		    for(int i = 0; i < messages.length; i++) {
		        processINTMessage(INTID, messages[i]);
		    }
		}
	}

    private void processINTMessage(Integer INTID, IS message) {        
        if(message != null) {
            if(message.hasRcon()) {
                processStdinMessage(message.getRcon());
            }
        }
    }

    private void processENV() {
        if(network.isConnectedToENV() == ConnectionStatus.CONNECTED) {
            ES[] messages;
            messages = network.receiveFromENV();
            if(messages == null) {
                System.err.println("Could not receive messages.");
            } else {
                for(int i = 0; i < messages.length; i++) {
                    processENVMessage(messages[i]);
                }
            }
        }
    }
    
    private void processENVMessage(ES message) {
        System.out.print(message.toString());
        if(message.hasHandshake() && message.getHandshake().getType() == ES.Handshake.Type.GOODBYE) {
            network.disconnectFromENV();
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
