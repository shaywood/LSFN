package com.wikispaces.lsfn.Ship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;

public class ShipServer implements Runnable {

    private ShipNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    ShipServer() {
        network = new ShipNetworking();
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * Treat this as the SHIP program's main entry point.
     */
    public void run() {
        try {
            network.openINTServer();
        } catch (IOException e1) {
            System.err.println("Failed to open the server socket.");
            e1.printStackTrace();
        }
        
        running = true;
        while(running) {
        	processUserInput();

            handshakeNewInterfaceConnections();
            processMessagesFromExistingINTConnections();
            
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
		// Handle existing connections
		HashMap<Integer, IS[]> all_messages = network.readAllFromINTs();
		Iterator<Integer> INTIDIterator = all_messages.keySet().iterator();
		while(INTIDIterator.hasNext()) {
		    Integer INTID = INTIDIterator.next();
		    IS[] messages = all_messages.get(INTID);
		    if(messages == null) continue;
		    for(int i = 0; i < messages.length; i++) {
		        processINTMessages(INTID, messages[i]);
		    }
		}
	}

	private void handshakeNewInterfaceConnections() {
		// Handle new connections
		Integer[] INT_IDs = network.getNewINTConnections();
		for(int i = 0; i < INT_IDs.length; i++) {
		    SI handshake = SI.newBuilder()
		            .setHandshake(SI.Handshake.newBuilder()
		                    .setType(SI.Handshake.Type.HELLO)
		                    .setPlayerID(INT_IDs[i])
		                    .build())
		            .build();
		    network.sendToINT(INT_IDs[i], handshake);
		}
	}
    
    private void processINTMessages(Integer INTID, IS message) {        
        if(message != null) {
            if(message.hasHandshake()) {
                switch(message.getHandshake()) {
                    case HELLO:
                        // Obsolete
                        break;
                    case GOODBYE:
                        network.disconnectINT(INTID);
                        break;
                }
            }
            
            if(message.hasCommand()) {
                IS.SHIP_ENV_command command = message.getCommand();
                switch(command.getType()) {
                    case CONNECT:
                    try {
                        network.connectToENV(command.getHost(), command.getPort());
                    } catch (IOException e) {
                        System.out.println("Failed to connect to server.");
                        e.printStackTrace();
                    }
                        break;
                    case DISCONNECT:
                        network.disconnectFromENV();
                        break;
                    case RECONNECT:
                        network.disconnectFromENV();
                        try {
                            network.connectToENV(command.getHost(), command.getPort());
                        } catch (IOException e) {
                            System.out.println("Failed to reconnect to server.");
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    private void process_ENV() {
        if(network.isConnectedtoENV()) {
            ES[] messages = null;
            try {
                messages = network.receiveFromENV();
            } catch (IOException e) {
                System.out.println("Failed to receive messages.");
                e.printStackTrace();
            }
            if(messages != null) {
                for(int i = 0; i < messages.length; i++) {
                    processENVMessage(messages[i]);
                }
            }
        }
    }
    
    private void processENVMessage(ES message) {
        if(message.hasHandshake()) {
            SI return_message = null;
            switch(message.getHandshake().getType()) {
                case HELLO:
                    return_message = SI.newBuilder()
                            .setStatus(SI.SHIP_ENV_status.newBuilder()
                                    .setState(SI.SHIP_ENV_status.State.CONNECTED)
                                    .setShipID(message.getHandshake().getShipID())
                                    .build())
                            .build();
                    break;
                case GOODBYE:
                    return_message = SI.newBuilder()
                            .setStatus(SI.SHIP_ENV_status.newBuilder()
                                    .setState(SI.SHIP_ENV_status.State.DISCONNECTED)
                                    .build())
                            .build();
                    network.disconnectFromENV();
                    break;
            }
            network.sendToAllINTs(return_message);
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
