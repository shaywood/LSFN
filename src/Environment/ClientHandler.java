package com.wikispaces.lsfn.Environment;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends ServerSocket implements Runnable {
    private HashMap<Integer, Socket> connections;
    private HashMap<Integer, BufferedReader> readers;
    private HashMap<Integer, PrintWriter> writers;
    private int next_connection_ID;
    
    private static final int default_port = 14612;
    
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    ClientHandler() throws IOException {
        super(default_port);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
    }
    
    /**
     * This class accepts all incoming connections from InterfaceClients.
     * @param port The port to listen for clients on.
     * @throws IOException
     */
    ClientHandler(int port) throws IOException {
        super(port);
        this.setSoTimeout(20);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
    }
    
    /**
     * As described by Runnable.
     */
    public void run() {
        while (!this.isClosed()) {
            try {
                // We don't need a sleep() because this is blocking.
                // This thread basically accepts all incoming connections.
                Socket incoming_connection = this.accept();
                if(!this.isClosed()) {
                    add_socket(incoming_connection);
                }
            } catch (SocketException e) {
                if(this.isClosed()) {
                    shut_down();                    
                } else {
                    // TODO
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets all of the messages that have been received from all connected clients.
     * @return The map key is the socket ID, the value is an array of messages. If a socket ID has no map entry, then no messages have been received from this client since the last read_all()
     */
    public HashMap<Integer, String[]> read_all() {
        Iterator<Integer> connection_iterator = connections.keySet().iterator();
        HashMap<Integer, String[]> messages = new HashMap<Integer, String[]>();
        while(connection_iterator.hasNext()) {
            Integer current_socket_ID = connection_iterator.next();
            Socket current_socket = connections.get(current_socket_ID);
            if(current_socket.isConnected() && !current_socket.isClosed()) {
                String[] message_list = receive(current_socket_ID);
                if(message_list != null) {
                    messages.put(current_socket_ID, message_list);
                }
            } else {
                remove_socket(current_socket_ID);
            }
        }
        return messages;
    }
    
    private String[] receive(Integer socket_ID) {
        Socket socket = connections.get(socket_ID);
        if(socket == null || !socket.isConnected() || socket.isClosed()) {
            remove_socket(socket_ID);
            return null;
        }
        BufferedReader current_reader = readers.get(socket_ID);
        ArrayList<String> message_list = new ArrayList<String>();
        
        try {
            while(current_reader.ready()) {
                message_list.add(current_reader.readLine());
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
            remove_socket(socket_ID);
            return null;
        }
        
        if(message_list.size() == 0) return null;
        
        return message_list.toArray(new String[0]);
    }
    
    /**
     * Sends a message to a specified socket if the specified socket exists.
     * @param socket_ID The ID of the socket to send the message to.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send(Integer socket_ID, String message) {
        Socket socket = connections.get(socket_ID);
        if(socket != null) {
            if(socket.isConnected()) {
                writers.get(socket_ID).println(message);
            } else {
                remove_socket(socket_ID);
            }
        }
    }
    
    /**
     * Sends the given message to every connected socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send_to_all(String message) {
        Iterator<Integer> socket_ID_iterator = connections.keySet().iterator();
        while(socket_ID_iterator.hasNext()) {
            send(socket_ID_iterator.next(), message);
        }
    }
    
    private synchronized void shut_down() {
        // We are shutting down the client handler,
        // close and remove all connections
        Integer[] remove_IDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < remove_IDs.length; i++) {
            remove_socket(remove_IDs[i]);
        }
    }
    
    private synchronized void add_socket(Socket socket) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        connections.put(next_connection_ID, socket);
        readers.put(next_connection_ID, br);
        writers.put(next_connection_ID, pw);
        System.out.println("New socket opened: " + next_connection_ID);
        next_connection_ID++;
    }
    
    /**
     * Disconnects and removes a socket from the ClientHandler.
     * @param socket_ID The ID of the socket to be removed.
     */
    public synchronized void remove_socket(Integer socket_ID) {
        Socket socket = connections.get(socket_ID);
        if(socket != null) {
            try {
                connections.get(socket_ID).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connections.remove(socket_ID);
            readers.remove(socket_ID);
            writers.remove(socket_ID);
            System.out.println("Closed socket: " + socket_ID);
        }
    }
}
