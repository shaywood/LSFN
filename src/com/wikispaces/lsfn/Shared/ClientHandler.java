package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.net.*;
import java.util.*;

import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;

public class ClientHandler implements Runnable {
    private ServerSocket server;
    private HashMap<Integer, SocketListener> connections;
    private int nextConnectionID;
    private List<Integer> newConnections;
    private List<Integer> controlledDisconnections;
    private List<Integer> uncontrolledDisconnections;
    
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    public ClientHandler() {
        server = null;
        connections = new HashMap<Integer, SocketListener>();
        nextConnectionID = 0;
        newConnections = new ArrayList<Integer>();
        controlledDisconnections = new ArrayList<Integer>();
        uncontrolledDisconnections = new ArrayList<Integer>();
    }
    
    public void open(int port) throws IOException {
        if(!isOpen()) {
            server = new ServerSocket(port);
        }
    }
    
    /**
     * As described by Runnable.
     */
    public void run() {
        while(isOpen()) {
            try {
                // We don't need a sleep() because this is blocking.
                // This thread basically accepts all incoming connections.
                Socket incomingConnection = server.accept();
                if(isOpen()) {
                    addSocket(incomingConnection);
                }
            } catch (SocketException e) {
                if(isOpen()) {
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
            if(socketListener.getConnectionStatus() == ConnectionStatus.CONNECTED) {
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
        if(isOpen()) {
            SocketListener socketListener = connections.get(socketID);
            if(socketListener.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                try {
                    socketListener.send(message);
                } catch (IOException e) {
                    removeSocket(socketID);
                }
            }
        }
    }
    
    /**
     * Sends the given message to every connected socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void sendToAll(byte[] message) {
        if(isOpen()) {
            Integer[] socketIDs = connections.keySet().toArray(new Integer[0]);
            for(int i = 0; i < socketIDs.length; i++) {
                send(socketIDs[i], message);
            }
        }
    }
    
    public synchronized void disconnect(Integer socketID) {
        if(isOpen()) {
            removeSocket(socketID);
        }
    }
    
    public synchronized void disconnectAll() {
        if(isOpen()) {
            // We are shutting down the client handler,
            // close and remove all connections
            Integer[] removeIDs = connections.keySet().toArray(new Integer[0]);
            for(int i = 0; i < removeIDs.length; i++) {
                removeSocket(removeIDs[i]);
            }
        }
    }
    
    public void close() {
        if(isOpen()) {
            disconnectAll();
            try {
                server.close();
            } catch (IOException e) {
                // We don't care if it fails, we're getting rid of this.
            }
            server = null;
        }
    }
    
    public boolean isOpen() {
        return server != null && !server.isClosed();
    }
    
    private synchronized void addSocket(Socket socket) {
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
            if(socketListener.getConnectionStatus() == ConnectionStatus.CONNECTED) socketListener.close();
            
            connections.remove(socketID);
            
            if(socketListener.getConnectionStatus() == ConnectionStatus.DISCONNECTED_CLEAN) {
                controlledDisconnections.add(socketID);
            } else if(socketListener.getConnectionStatus() == ConnectionStatus.DISCONNECTED_UNCLEAN) {
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
