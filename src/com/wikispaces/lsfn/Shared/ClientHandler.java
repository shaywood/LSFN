package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends ServerSocket implements Runnable {    
    private HashMap<Integer, SocketListener> connections;
    private int nextConnectionID;
    private List<Integer> newConnections;
    private List<Integer> controlledDisconnections;
    private List<Integer> uncontrolledDisconnections;
    
    private static final int defaultPort = 14612;
    
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    public ClientHandler() throws IOException {
        super(defaultPort);
        commonSetup();
    }
    
    /**
     * This class accepts all incoming connections from InterfaceClients.
     * @param port The port to listen for clients on.
     * @throws IOException
     */
    public ClientHandler(int port) throws IOException {
        super(port);
        commonSetup();
    }
    
    private void commonSetup() {
        connections = new HashMap<Integer, SocketListener>();
        nextConnectionID = 0;
        newConnections = new ArrayList<Integer>();
        controlledDisconnections = new ArrayList<Integer>();
        uncontrolledDisconnections = new ArrayList<Integer>();
    }
    
    /**
     * As described by Runnable.
     */
    public void run() {
        while (!this.isClosed()) {
            try {
                // We don't need a sleep() because this is blocking.
                // This thread basically accepts all incoming connections.
                Socket incomingConnection = this.accept();
                if(!this.isClosed()) {
                    add_socket(incomingConnection);
                }
            } catch (SocketException e) {
                if(!this.isClosed()) {
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
    public HashMap<Integer, byte[][]> readAll() {
        Integer[] socketIDs = connections.keySet().toArray(new Integer[0]);
        HashMap<Integer, byte[][]> messages = new HashMap<Integer, byte[][]>();
        
        for(int i = 0; i < socketIDs.length; i++) {
            Integer currentSocketID = socketIDs[i];
            SocketListener socketListener = connections.get(currentSocketID);
            if(socketListener.isConnected()) {
                try {
                    byte[][] messageSet = socketListener.receive();
                    messages.put(currentSocketID, messageSet);
                } catch (IOException e) {
                    removeSocket(currentSocketID);
                }
            } else {
                removeSocket(currentSocketID);
            }
        }
        
        return messages;
    }
    
    /**
     * Sends a message to a specified socket if the specified socket exists.
     * @param socket_ID The ID of the socket to send the message to.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send(Integer socketID, byte[] message) {
        SocketListener socketListener = connections.get(socketID);
        if(socketListener != null && socketListener.isConnected()) {
            try {
                socketListener.send(message);
            } catch (IOException e) {
                removeSocket(socketID);
            }
        }
    }
    
    /**
     * Sends the given message to every connected socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void sendToAll(byte[] message) {
        Integer[] socketIDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < socketIDs.length; i++) {
            send(socketIDs[i], message);
        }
    }
    
    public synchronized void disconnect(Integer socketID) {
        removeSocket(socketID);
    }
    
    public synchronized void disconnectAll() {
        // We are shutting down the client handler,
        // close and remove all connections
        Integer[] removeIDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < removeIDs.length; i++) {
            removeSocket(removeIDs[i]);
        }
    }
    
    private synchronized void add_socket(Socket socket) {
        try {
            SocketListener socketListener = new SocketListener(socket);
            connections.put(nextConnectionID, socketListener);
            newConnections.add(nextConnectionID);
            
            System.out.println("New socket opened: " + nextConnectionID);
            nextConnectionID++;
        } catch (IOException e) {
            // We simply ignore the new socket. The garbage collector will take care of this end of the connection, liveness testing the other. 
        }
    }
    
    /**
     * Disconnects and removes a socket from the ClientHandler.
     * @param socket_ID The ID of the socket to be removed.
     */
    private synchronized void removeSocket(Integer socketID) {
        SocketListener socketListener = connections.get(socketID);
        if(socketListener != null) {
            if(socketListener.isConnected()) socketListener.close();
            
            connections.remove(socketID);
            
            if(socketListener.wasCleanDisconnect()) {
                controlledDisconnections.add(socketID);
            } else {
                uncontrolledDisconnections.add(socketID);
            }
            
            System.out.println("Closed socket: " + socketID);
        }
    }
    
    public synchronized Integer[] getNewConnections() {
        Integer[] IDs = newConnections.toArray(new Integer[0]);
        newConnections.clear();
        return IDs;
    }
    
    public synchronized Integer[] getControlledDisconnections() {
        Integer[] IDs = controlledDisconnections.toArray(new Integer[0]);
        controlledDisconnections.clear();
        return IDs;
    }
    
    public synchronized Integer[] getUncontrolledDisconnections() {
        Integer[] IDs = uncontrolledDisconnections.toArray(new Integer[0]);
        uncontrolledDisconnections.clear();
        return IDs;
    }
}
