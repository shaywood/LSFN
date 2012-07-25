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

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.Shared.Messaging.Accelerate;
import com.wikispaces.Shared.Messaging.AvailableSubscriptionsList;
import com.wikispaces.Shared.Messaging.NoMessageBuilderDefinedException;
import com.wikispaces.Shared.Messaging.NoMessageParserDefinedException;
import com.wikispaces.Shared.Messaging.Message;
import com.wikispaces.Shared.Messaging.MessageFactory;
import com.wikispaces.Shared.Messaging.MessageSimplifier;
import com.wikispaces.Shared.Messaging.MessageBuilderFactory;
import com.wikispaces.Shared.Messaging.MessageParserFactory;
import com.wikispaces.Shared.Messaging.SubscriptionRequest;
import com.wikispaces.Shared.Messaging.UnavailableSubscriptionException;
import com.wikispaces.Shared.Messaging.MessageFactory.SubscribeableNotFoundException;
import com.wikispaces.Shared.Messaging.MessageParser.PublishFailedException;
import com.wikispaces.lsfn.Shared.ClientHandler;
import com.wikispaces.lsfn.Shared.LSFN;
import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SE.Ship_movement;
import com.wikispaces.lsfn.Shared.Listener;

public class ShipServer implements Runnable {

    private ClientHandler INT_server;
    private Thread INT_server_thread;
    private Listener ENV_client;
    private Thread ENV_client_thread;
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
        INT_server = null;
        INT_server_thread = null;
        ENV_client = null;
        ENV_client_thread = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    /**
     * Treat this as the SHIP program's main entry point.
     */
    public void run() {
        start_INT_server();
        
        running = true;
        while(running) {
        	process_user_input();

            if(INT_server != null) {
                handshake_new_interface_connections();
                process_messages_from_existing_INT_connections();
            }
            
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
        
        stop_ENV_client(true);
        stop_INT_server();
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
    		INT_server.send(id, builder.build().toByteArray());
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
	    	
	    	ENV_client.send(accelerate_message.build().toByteArray());
    	}
	}

	private void process_user_input() {
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
		HashMap<Integer, byte[][]> all_messages = INT_server.read_all();
		Iterator<Integer> INT_ID_iterator = all_messages.keySet().iterator();
		while(INT_ID_iterator.hasNext()) {
		    Integer INT_ID = INT_ID_iterator.next();
		    byte[][] messages = all_messages.get(INT_ID);
		    if(messages == null) continue;
		    for(int i = 0; i < messages.length; i++) {
		        process_INT_message(INT_ID, messages[i]);
		    }
		}
	}

	private void handshake_new_interface_connections() {
		// Handle new connections
		Integer[] INT_IDs = INT_server.get_new_connections();
		for(int i = 0; i < INT_IDs.length; i++) {
		    SI handshake = SI.newBuilder()
		            .setHandshake(SI.Handshake.newBuilder()
		                    .setType(SI.Handshake.Type.HELLO)
		                    .setPlayerID(INT_IDs[i])
		                    .build())
		            .build();
		    INT_server.send(INT_IDs[i], handshake.toByteArray());
		}
	}
    
    private void process_INT_message(Integer INT_ID, byte[] message) {
        IS parsed_message = null;
        try {
            parsed_message = LSFN.IS.parseFrom(message);
            System.out.print(parsed_message.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        
        if(parsed_message != null) {
            if(parsed_message.hasHandshake()) {
                switch(parsed_message.getHandshake()) {
                    case HELLO:
                        // Obsolete
                        break;
                    case GOODBYE:
                        INT_server.remove_socket(INT_ID);
                        break;
                }
            }
            
            if(parsed_message.hasCommand()) {
                IS.SHIP_ENV_command command = parsed_message.getCommand();
                switch(command.getType()) {
                    case CONNECT:
                        start_ENV_client(command.getHost(), command.getPort());
                        break;
                    case DISCONNECT:
                        stop_ENV_client(true);
                        break;
                    case RECONNECT:
                        stop_ENV_client(true);
                        start_ENV_client(command.getHost(), command.getPort());
                        break;
                }
            }
            
            if(parsed_message.getAvailableSubscriptionsList()) {
            	INT_server.send(INT_ID, new AvailableSubscriptionsList(subscribeable_factory).build_message(INT_ID).toByteArray());
            }
            if(parsed_message.hasSubscribe()) {
            	try {
					interface_client_subscriptions.subscribe(INT_ID, subscriber.parse_message(parsed_message));
				} catch (SubscribeableNotFoundException e) {
					e.printStackTrace();
				} catch (UnavailableSubscriptionException e) {
					e.printStackTrace();
				}
            }
            if(parsed_message.hasInputUpdates()) {
        		try {
					Set<Message> updates = receiver.parse_subscription_data(parsed_message.getInputUpdates());
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


	private void start_INT_server() {
        try {
            INT_server = new ClientHandler();
        } catch(IOException e) {
            INT_server = null;
        }
        
        // Check if we have successfully created a ClientHandler to serve as a host for INTs
        if(INT_server != null) {
            INT_server_thread = new Thread(INT_server);
            INT_server_thread.start();
        }
    }
    
    private void stop_INT_server() {
        if(INT_server != null) {
            try {
                INT_server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Send a goodbye to each connected client
            SI goodbye_message = SI.newBuilder()
                    .setHandshake(SI.Handshake.newBuilder()
                            .setType(SI.Handshake.Type.GOODBYE)
                            .build())
                    .build();
            INT_server.send_to_all(goodbye_message.toByteArray());
            
            // Close the server
            try {
                INT_server_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            INT_server = null;
            INT_server_thread = null;
        }
    }
    
    private void process_ENV() {
        if(ENV_client != null) {
            byte[][] messages = ENV_client.get_messages();
            for(int i = 0; i < messages.length; i++) {
                process_ENV_message(messages[i]);
            }
        }
    }
    
    private void process_ENV_message(byte[] message) {
        ES parsed_message = null;
        try {
            parsed_message = ES.parseFrom(message);
            System.out.print(parsed_message.toString());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        
        if(parsed_message != null) {
            if(parsed_message.hasHandshake()) {
                SI return_message = null;
                switch(parsed_message.getHandshake().getType()) {
                    case HELLO:
                        return_message = SI.newBuilder()
                                .setStatus(SI.SHIP_ENV_status.newBuilder()
                                        .setState(SI.SHIP_ENV_status.State.CONNECTED)
                                        .setShipID(parsed_message.getHandshake().getShipID())
                                        .build())
                                .build();
                        break;
                    case GOODBYE:
                        return_message = SI.newBuilder()
                                .setStatus(SI.SHIP_ENV_status.newBuilder()
                                        .setState(SI.SHIP_ENV_status.State.DISCONNECTED)
                                        .build())
                                .build();
                        stop_ENV_client(false);
                        break;
                }
                INT_server.send_to_all(return_message.toByteArray());
            }
            
            if(parsed_message.hasPositions()) {
                Iterator<ES.Ship_positions.Ship_position> parsed_positions_iterator = parsed_message.getPositions().getPositionsList().iterator();
                SI.Ship_positions.Builder positions_builder = SI.Ship_positions.newBuilder();
                while(parsed_positions_iterator.hasNext()) {
                    ES.Ship_positions.Ship_position pos = parsed_positions_iterator.next();
                    positions_builder.addPositions(SI.Ship_positions.Ship_position.newBuilder()
                            .setShipID(pos.getShipID())
                            .addAllCoordinates(pos.getCoordinatesList())
                            .build());
                }
                INT_server.send_to_all(SI.newBuilder().setPositions(positions_builder.build()).build().toByteArray());
            }
        }
    }
    
    private void start_ENV_client(String host, int port) {
        try {
            ENV_client = new Listener(host, port);
        } catch (IOException e) {
            ENV_client = null;
        }
        
        if(ENV_client != null) {
            ENV_client_thread = new Thread(ENV_client);
            ENV_client_thread.start();
        }
    }
    
    private void stop_ENV_client(boolean send_goodbye) {
        if(ENV_client != null) {
            try {
                ENV_client_thread.interrupt();
                ENV_client_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if(send_goodbye) {
                SE handshake = SE.newBuilder().setHandshake(SE.Handshake.GOODBYE).build();
                ENV_client.send(handshake.toByteArray());
            }
            
            try {
                ENV_client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            ENV_client = null;
            ENV_client_thread = null;
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
