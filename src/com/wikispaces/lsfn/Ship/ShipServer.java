package com.wikispaces.lsfn.Ship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;

public class ShipServer implements Runnable {
    private ShipNetworking network;
    private boolean running;
    private BufferedReader stdin;
    private InterfaceManager INTManager;
    
    ShipServer() {
        network = new ShipNetworking();
        stdin = new BufferedReader(new InputStreamReader(System.in));
        INTManager = new InterfaceManager(network, 500);
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

            INTManager.handleNewConnections();
            processMessagesFromExistingINTConnections();
            handleDisconnections();
            
            
            // Get messages from the ENV (if it's connected)
            process_ENV();
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.closeINTServer();
        network.disconnectFromENV();
    }
    

	private void handleDisconnections() {
        Integer[] dcs = network.getControlledINTDisconnections();
        for(int i = 0; i < dcs.length; i++) {
            INTManager.handleDisconnect(dcs[i]);
        }
        
        dcs = network.getUncontrolledINTDisconnections();
        for(int i = 0; i < dcs.length; i++) {
            INTManager.handleDisconnect(dcs[i]);
        }
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
    
	private void processMessagesFromExistingINTConnections() {
		HashMap<Integer, IS[]> allMessages = network.readAllFromINTs();
		for(Integer INTID : allMessages.keySet()) {
		    IS[] messages = allMessages.get(INTID);
		    if(messages == null) continue;
		    
		    // This section does checks on the handshake of the client.
		    if(!INTManager.verify(INTID)) {
		        // If a connection has not been verified
		        if(messages[0].hasHandshake()) {
		            // allow it to handshake.
                    INTManager.handleHandshake(INTID, messages[0].getHandshake());
                    // If the handshake is successful, start parsing it's messages.
                    if(!INTManager.verify(INTID)) {
                        // If the handshake is unsuccessful, disconnect it.
                        network.disconnectINT(INTID);
                        continue;
                    }
                } else {
                    // If it won't shake hands, disconnect it.
                    network.disconnectINT(INTID);
                    continue;
                }
		    }
		    
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

    private void process_ENV() {
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
