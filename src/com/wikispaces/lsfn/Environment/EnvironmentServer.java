package com.wikispaces.lsfn.Environment;

import com.wikispaces.lsfn.Shared.*;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import java.io.*;
import java.util.*;

public class EnvironmentServer implements Runnable {
    private EnvironmentNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    EnvironmentServer() {
        network = new EnvironmentNetworking();
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public void run() {
        try {
            network.openSHIPServer();
        } catch (IOException e1) {
            System.out.println("Failed to open SHIP server.");
            e1.printStackTrace();
        }
        
        running = true;
        while(running) {
        	processUserInput();
            
        	handshakeNewSHIPConnections();
        	processMessagesFromExistingSHIPConnections();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO
                e.printStackTrace();
            }
        }
        
        network.closeSHIPServer();
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
        if(message.equals("stop")) running = false;
    }
    
    private void handshakeNewSHIPConnections() {
        Integer[] SHIP_IDs = network.getNewSHIPConnections();
        for(int i = 0; i < SHIP_IDs.length; i++) {
            // This creates a new ship and adds it to the collection that is staic to Ship.
            Ship ship = new Ship(SHIP_IDs[i], 0, 0);
            
            // Send a handshake to the SHIP
            LSFN.ES handshake = LSFN.ES.newBuilder()
                    .setHandshake(LSFN.ES.Handshake.newBuilder()
                            .setType(LSFN.ES.Handshake.Type.HELLO)
                            .setShipID(ship.get_ID())
                            .build())
                    .build();
            network.sendToSHIP(SHIP_IDs[i], handshake);
        }
    }
    
    private void processMessagesFromExistingSHIPConnections() {    	
        HashMap<Integer, SE[]> messages = network.readAllFromSHIPs();
        Iterator<Integer> message_iterator = messages.keySet().iterator();
        while(message_iterator.hasNext()) {
            Integer message_ID = message_iterator.next();
            SE[] message_array = messages.get(message_ID);
            for(int i = 0; i < message_array.length; i++) {
                processSHIPMessage(message_ID, message_array[i]);
            }
        }
    }
    
    private void processSHIPMessage(Integer SHIPID, SE message) {
        if(message != null) {
            if(message.hasHandshake()) {
                switch(message.getHandshake()) {
                    case HELLO:
                        // Obsolete
                        break;
                    case GOODBYE:
                        network.disconnectSHIP(SHIPID);
                        break;
                }
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnvironmentServer().run();
    }
}
