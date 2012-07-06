package com.wikispaces.lsfn.Interface;

import com.wikispaces.lsfn.Interface.Display2D.MapDisplay;
import com.wikispaces.lsfn.Interface.Model.*;
import com.wikispaces.lsfn.Shared.*;
import com.wikispaces.lsfn.Shared.LSFN.*;

import com.google.protobuf.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class InterfaceClient {
    private Listener SHIP_client;
    private Thread SHIP_client_thread;
    private boolean running;
    private BufferedReader stdin;
    private Subscribe subscriber;
	
	KnownSpace world;
	MapDisplay display;
    
    InterfaceClient() {
        SHIP_client = null;
        SHIP_client_thread = null;
        stdin = new BufferedReader(new InputStreamReader(System.in));
		
		world = new DummyUniverse();
		display = new MapDisplay(world);
    }
    
    int cycle_time_ms = 20;
    double cycle_time = ((double)cycle_time_ms)/1000.0;
 
    public void run() {      
        running = true;
        while(running) {
            process_user_input();
            process_incoming_SHIP_messages();
			
			world.update(cycle_time);
			display.repaint();
            
            try {
                Thread.sleep(cycle_time_ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // When we shut down, we close the SHIP_client and join the thread.
        System.out.println("Shutting down.");
        stop_SHIP_client(true);
        System.exit(0);
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
        String[] parts = message.split(" ");
        int num_parts = parts.length;
        
        if(message.equals("stop")) {
            running = false;
        } else if(num_parts >= 1 && parts[0].equals("connect")) { // "connect remote" connects the ship to the environment server.
            if(num_parts == 4 && parts[1].equals("remote")) {
                IS sendable = IS.newBuilder()
                        .setCommand(IS.SHIP_ENV_command.newBuilder()
                                .setType(IS.SHIP_ENV_command.Type.CONNECT)
                                .setHost(parts[2])
                                .setPort(Integer.parseInt(parts[3]))
                                .build())
                        .build();
                SHIP_client.send(sendable.toByteArray());
            } else if(num_parts == 3) { // "connect" connects the interface to the ship. Port 14613 is default on the Ship server.
                start_SHIP_client(parts[1], Integer.parseInt(parts[2]));
            }
        } else if(message.equals("disconnect remote")) { // "disconnect remote" disconnect the ship from the environment server.
                IS sendable = IS.newBuilder()
                        .setCommand(IS.SHIP_ENV_command.newBuilder()
                                .setType(IS.SHIP_ENV_command.Type.DISCONNECT)
                                .build())
                        .build();
                SHIP_client.send(sendable.toByteArray());
        } else if(message.equals("disconnect")) {
            stop_SHIP_client(true);
        } else {
        	System.out.println("Unknown message: " + message);
        }
        
    }
    
    private void process_incoming_SHIP_messages() {
        if(SHIP_client != null) {
            byte[][] messages = SHIP_client.get_messages();
            for(int i = 0; i < messages.length; i++) {
                process_SHIP_message(messages[i]);
            }
        }
    }
        
    private void process_SHIP_message(byte[] message) {
        try {
            SI parsed_message = SI.parseFrom(message);
            System.out.print(parsed_message.toString());
            if(parsed_message.hasHandshake() && parsed_message.getHandshake().getType() == SI.Handshake.Type.GOODBYE) {
                stop_SHIP_client(false);
            }
            if(parsed_message.hasSubscriptionsAvailable()) {
            	subscriber = new Subscribe(new ListAvailableSubscriptions().parse_message(parsed_message));
            	request_default_subscriptions();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (SubscribeableNotFoundException e) {
        	e.printStackTrace();
        } catch (UnavailableSubscriptionExeption e) {
	    	e.printStackTrace();
	    }
    }
    
    List<Subscribeable> default_subscriptions = Arrays.asList(Subscribeable.TEST); // this probably belongs somewhere else
	private void request_default_subscriptions() throws UnavailableSubscriptionExeption {
		SHIP_client.send(subscriber.build_message(default_subscriptions).toByteArray());
	}

	private void start_SHIP_client(String host, int port) {
        try {
            SHIP_client = new Listener(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the SHIP.");
        }
        
        // Check if socket creation succeeded
        if(SHIP_client != null) {
            SHIP_client_thread = new Thread(SHIP_client);
            SHIP_client_thread.start();
        }
        
        on_connect();
    }
    
    private void on_connect() {
        SHIP_client.send(new RequestAvailableSubscriptions().build_message().toByteArray());
    }
    
    private void stop_SHIP_client(boolean send_goodbye) {
        if(SHIP_client != null) {
            try {
                SHIP_client_thread.interrupt();
                SHIP_client_thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            if(send_goodbye) {
                IS handshake = IS.newBuilder().setHandshake(IS.Handshake.GOODBYE).build();
                SHIP_client.send(handshake.toByteArray());
            }
            
            try {
                SHIP_client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            SHIP_client = null;
            SHIP_client_thread = null;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new InterfaceClient().run();
    }

}
