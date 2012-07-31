package com.wikispaces.lsfn.Interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;

//import com.wikispaces.lsfn.Interface.Display2D.MapDisplay;
//import com.wikispaces.lsfn.Interface.Model.KnownSpace;
//import com.wikispaces.lsfn.Interface.Model.Ship;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;
//import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates;
//import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Messaging.AvailableSubscriptionsList;
import com.wikispaces.lsfn.Shared.Messaging.Message;
//import com.wikispaces.lsfn.Shared.Messaging.MessageBuilderFactory;
import com.wikispaces.lsfn.Shared.Messaging.MessageFactory;
import com.wikispaces.lsfn.Shared.Messaging.MessageParser.PublishFailedException;
import com.wikispaces.lsfn.Shared.Messaging.MessageParserFactory;
//import com.wikispaces.lsfn.Shared.Messaging.MessageSimplifier;
//import com.wikispaces.lsfn.Shared.Messaging.NoMessageBuilderDefinedException;
import com.wikispaces.lsfn.Shared.Messaging.NoMessageParserDefinedException;
import com.wikispaces.lsfn.Shared.Messaging.SubscriptionRequest;
import com.wikispaces.lsfn.Shared.Messaging.Test;
import com.wikispaces.lsfn.Shared.Messaging.UnavailableSubscriptionException;

public class InterfaceClient {
    private InterfaceNetworking network;
    private boolean running;
    private BufferedReader stdin;
    private SubscriptionRequest subscriber;
    private MessageFactory subscribeable_factory = new MessageFactory();
    private MessageParserFactory receiver = new MessageParserFactory(subscribeable_factory, new TestParser());
    /*private MessageBuilderFactory transmitter = new MessageBuilderFactory(
    		new AccelerateBuilder());
    private BlockingQueue<Message> player_input_queue = new LinkedBlockingQueue<Message>();
	
	KnownSpace world = new KnownSpace();
	MapDisplay display = new MapDisplay(this, player_input_queue, world);
	
	ShipPositionParser positionParser = new ShipPositionParser(world);*/
    
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
            
        	//process_player_input();
			
			//world.update(cycle_time);
			//display.repaint();
            
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
    
    /*private void process_player_input() {
    	List<Message> commands = new ArrayList<Message>();
		player_input_queue.drainTo(commands);
		
		commands = new MessageSimplifier().merge(commands);
		
		List<Subscription_update> updates = new ArrayList<Subscription_update>();
		for(Message c : commands) {
			// We should also do any local model updates needed here.
			try {
				updates.add((transmitter.get_builder(c).build_subscription_update(c)));
			} catch (NoMessageBuilderDefinedException e) {
				e.printStackTrace();
			}
		}
		
		if(updates.size() > 0) {
			IS.Builder message_builder = IS.newBuilder();
			Subscription_updates.Builder subscription_builder = Subscription_updates.newBuilder();
			subscription_builder.addAllUpdates(updates);
			message_builder.setInputUpdates(subscription_builder.build());
			try {
                network.sendToSHIP(message_builder.build());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
	}*/

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
                    on_connect();
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
        try {
            System.out.print(message.toString());
            if(message.hasHandshake() && message.getHandshake().getType() == SI.Handshake.Type.GOODBYE) {
                network.disconnectFromSHIP();
            }
            if(message.hasSubscriptionsAvailable()) {
            	subscriber = new SubscriptionRequest(subscribeable_factory, new AvailableSubscriptionsList(subscribeable_factory).parse_message(message));
            	request_default_subscriptions();
            }
            /*if(message.hasStatus() && message.getStatus().hasShipID()) {
            	world.set_our_ship(new Ship(message.getStatus().getShipID()));
            }
            if(message.hasPositions()) {
            	positionParser.update_model_with_data(message.getPositions());
            }*/
            if(message.hasOutputUpdates()) {
            	receiver.parse_subscription_data(message.getOutputUpdates());
            }
        } catch (MessageFactory.SubscribeableNotFoundException e) {
        	e.printStackTrace();
        } catch (UnavailableSubscriptionException e) {
	    	e.printStackTrace();
	    } catch (PublishFailedException e) {
			e.printStackTrace();
		} catch (NoMessageParserDefinedException e) {
			e.printStackTrace();
		}
    }

    List<Message> default_subscriptions = Arrays.asList((Message)new Test()); // this probably belongs somewhere else
	private void request_default_subscriptions() throws UnavailableSubscriptionException {
		try {
            network.sendToSHIP(subscriber.build_message(default_subscriptions));
        } catch (IOException e) {
            System.err.println("Could not send subscribe message.");
            e.printStackTrace();
        }
	}

	private void on_connect() {
        try {
            network.sendToSHIP(new RequestAvailableSubscriptions().build_message());
        } catch (IOException e) {
            System.err.println("Could not send subscription get request.");
            e.printStackTrace();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new InterfaceClient().run();
    }

}
