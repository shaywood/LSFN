package com.wikispaces.lsfn.Ship;

import com.wikispaces.lsfn.Shared.*;
import com.wikispaces.lsfn.Shared.LSFN.*;
import com.google.protobuf.*;
import java.io.*;
import java.util.*;

public class ShipServer implements Runnable {

    private ShipNetworking network;
    private boolean running;
    private BufferedReader stdin;
    private Subscriptions interface_client_subscriptions = new Subscriptions();
    private Subscribe subscriber = new Subscribe(Subscribeable.get_all_available_subscribeables());
    
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
            process_messages_from_existing_INT_connections();
            
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
                process_stdin_message(stdin.readLine());
            }
        } catch (IOException e) {
            System.err.println("Failed to read from stdin.");
            e.printStackTrace();
            running = false;
        }
    }
    
    private void process_stdin_message(String message) {
        if(message.equals("stop")) running = false;
    }
    
	private void process_messages_from_existing_INT_connections() {
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
            
            if(message.getAvailableSubscriptionsList()) {
            	network.sendToINT(INTID, new ListAvailableSubscriptions().build_message(INTID));
            }
            if(message.hasSubscribe()) {
            	try {
					interface_client_subscriptions.subscribe(INTID, subscriber.parse_message(message));
				} catch (SubscribeableNotFoundException e) {
					e.printStackTrace();
				} catch (UnavailableSubscriptionExeption e) {
					e.printStackTrace();
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
                    process_ENV_message(messages[i]);
                }
            }
        }
    }
    
    private void process_ENV_message(ES message) {
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
        
        if(message.hasPositions()) {
            Iterator<ES.Ship_positions.Ship_position> parsed_positions_iterator = message.getPositions().getPositionsList().iterator();
            SI.Ship_positions.Builder positions_builder = SI.Ship_positions.newBuilder();
            while(parsed_positions_iterator.hasNext()) {
                ES.Ship_positions.Ship_position pos = parsed_positions_iterator.next();
                positions_builder.addPositions(SI.Ship_positions.Ship_position.newBuilder()
                        .setShipID(pos.getShipID())
                        .addAllCoordinates(pos.getCoordinatesList())
                        .build());
            }
            network.sendToAllINTs(SI.newBuilder().setPositions(positions_builder.build()).build());
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
