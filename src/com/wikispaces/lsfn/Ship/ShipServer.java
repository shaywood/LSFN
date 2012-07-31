package com.wikispaces.lsfn.Ship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import com.wikispaces.lsfn.Shared.LSFN.SE.Ship_movement;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.Messaging.Accelerate;
import com.wikispaces.lsfn.Shared.Messaging.AvailableSubscriptionsList;
import com.wikispaces.lsfn.Shared.Messaging.Message;
import com.wikispaces.lsfn.Shared.Messaging.MessageBuilderFactory;
import com.wikispaces.lsfn.Shared.Messaging.MessageFactory;
import com.wikispaces.lsfn.Shared.Messaging.MessageFactory.SubscribeableNotFoundException;
import com.wikispaces.lsfn.Shared.Messaging.MessageParser.PublishFailedException;
import com.wikispaces.lsfn.Shared.Messaging.MessageParserFactory;
import com.wikispaces.lsfn.Shared.Messaging.MessageSimplifier;
import com.wikispaces.lsfn.Shared.Messaging.NoMessageBuilderDefinedException;
import com.wikispaces.lsfn.Shared.Messaging.NoMessageParserDefinedException;
import com.wikispaces.lsfn.Shared.Messaging.SubscriptionRequest;
import com.wikispaces.lsfn.Shared.Messaging.UnavailableSubscriptionException;

public class ShipServer implements Runnable {

    private ShipNetworking network;
    private boolean running;
    private BufferedReader stdin;
    private Subscriptions interface_client_subscriptions = new Subscriptions();
    private MessageFactory subscribeable_factory = new MessageFactory();
    private SubscriptionRequest subscriber = new SubscriptionRequest(subscribeable_factory, subscribeable_factory.get_outputs());
    private SubscriptionPublisher publisher = new SubscriptionPublisher(interface_client_subscriptions, new MessageBuilderFactory(new TestBuilder()));
    private MessageParserFactory receiver = new MessageParserFactory(subscribeable_factory,	
    		new AccelerateParser());
    
    private BlockingQueue<Message> updates_for_INT = new LinkedBlockingQueue<Message>();
    private BlockingQueue<Message> updates_for_ENV = new LinkedBlockingQueue<Message>();
	private MessageSimplifier subscribeable_simplifier = new MessageSimplifier();
    
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
            
            // Do we want to move this inside process_ENV?
            publish_updates_to_INT();
            publish_updates_to_ENV();
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        network.closeINTServer();
        network.disconnectFromENV();
    }
    
	private void publish_updates_to_INT() {
    	Set<Integer> INT_ids =  interface_client_subscriptions.get_subscribers();
    	List<Message> updates = new ArrayList<Message>();
    	updates_for_INT.drainTo(updates);
    	updates = subscribeable_simplifier.merge(updates);
    	
    	for(Integer id : INT_ids) { 
    		SI.Builder builder = SI.newBuilder();
    		try {
				publisher.add_subscription_outputs_data(builder, id, updates);
			} catch (UnknownInterfaceClientException e) {
				e.printStackTrace();
			} catch (NoMessageBuilderDefinedException e) {
				e.printStackTrace();
			}
    		network.sendToINT(id, builder.build());
    	}
	}
	
	int accelerate_id = new Accelerate(UnitDirection.NOWHERE).get_id();
    private void publish_updates_to_ENV() {
    	List<Message> updates = new ArrayList<Message>();
    	updates_for_ENV.drainTo(updates);
    	// updates = subscribeable_simplifier.merge(updates); // ToDo: not necessary at this point, since all our ENV updates are coming via the INT which will already have  merged them. We should think it through more.
    	
    	if(updates.size() > 0) {
    		SE.Builder accelerate_message = SE.newBuilder();
    		
	    	for(Message s : updates) {
	    		if(s.get_id() == accelerate_id) { // this is a nasty solution. Look into doing better in the next version.
					Accelerate a = (Accelerate)s;
					accelerate_message.setMovement(Ship_movement.newBuilder()
						.addAxisAccel(a.get_direction().get_north_south())
						.addAxisAccel(a.get_direction().get_east_west()));
	    		}
	    	}
	    	
	    	try {
                network.sendToENV(accelerate_message.build());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            	network.sendToINT(INTID, new AvailableSubscriptionsList(subscribeable_factory).build_message(INTID));
            }
            if(message.hasSubscribe()) {
            	try {
					interface_client_subscriptions.subscribe(INTID, subscriber.parse_message(message));
				} catch (SubscribeableNotFoundException e) {
					e.printStackTrace();
				} catch (UnavailableSubscriptionException e) {
					e.printStackTrace();
				}
            }
            if(message.hasInputUpdates()) {
        		try {
					Set<Message> updates = receiver.parse_subscription_data(message.getInputUpdates());
					updates_for_ENV.addAll(updates); // Dumping updates from INT straight to ENV for now. We may want to do more here later.
				} catch (PublishFailedException e) {
					e.printStackTrace();
				} catch (NoMessageParserDefinedException e) {
					e.printStackTrace();
				} catch (SubscribeableNotFoundException e) {
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
