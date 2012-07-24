package com.wikispaces.lsfn.Environment;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.*;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import com.wikispaces.lsfn.Shared.LSFN.*;
import java.io.*;
import java.util.*;

public class EnvironmentServer implements Runnable {
    private EnvironmentNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    private Space space;
    
    EnvironmentServer() {
        network = new EnvironmentNetworking();
        stdin = new BufferedReader(new InputStreamReader(System.in));
        space = new Space(1000, 1000);
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
            
            // Run the tick() functions of everything in space
            space.tick();
            
            // Send back state output
            sendPositionOutput();
            
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
            LSFN.ES handshake = LSFN.ES.newBuilder()
                    .setHandshake(LSFN.ES.Handshake.newBuilder()
                            .setType(LSFN.ES.Handshake.Type.HELLO)
                            .setShipID(SHIP_IDs[i])
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
            
            if(message.hasMovement()) {
                Ship.data_from_SHIPs(SHIPID, message.getMovement());
            }
        }
    }
    
    private void sendPositionOutput() {
        ES state_output = ES.newBuilder()
                .setPositions(Ship.get_proto_positions())
                .build();
        network.sendToAllSHIPs(state_output);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnvironmentServer().run();
    }
}
