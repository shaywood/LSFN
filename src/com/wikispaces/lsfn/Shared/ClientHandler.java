package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends ServerSocket implements Runnable {
    private class SocketData {
        public Socket socket;
        public SocketBitJockey jockey;
        public TimeoutManager timeoutManager;
    }
    
    private HashMap<Integer, SocketData> connections;
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
        connections = new HashMap<Integer, SocketData>();
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
            SocketData socketData = connections.get(currentSocketID);
            
            if(socketData.timeoutManager.shouldTimeout()) {
                removeSocket(currentSocketID, false);
            } else {
                try {
                    byte[][] messageArray = socketData.jockey.readMessages();
                    
                    if(messageArray != null) {
                        socketData.timeoutManager.receiveOccured();
                        ArrayList<byte[]> messageList = new ArrayList<byte[]>();
                        for(int j = 0; j < messageArray.length; j++) {
                            if(!checkMessageForSignal(currentSocketID, socketData, messageArray[j])) {
                                messageList.add(messageArray[j]);
                            }
                        }
                    
                        messages.put(currentSocketID, messageList.toArray(new byte[0][]));
                    }
                    
                } catch (IOException e) {
                    removeSocket(currentSocketID, false);
                }
            }
        }
        
        return messages;
    }
    
    private boolean checkMessageForSignal(Integer socketID, SocketData socketData, byte[] signalBytes) {
        if(signalBytes.length != 1) return false;
        switch ((char)signalBytes[0]) {
        case 'P':
            // The pong signal has been received
            // We've actually already done what we needed (resetting the receive counter)
            return true;
        case 'D':
            // The disconnect signal has been received
            removeSocket(socketID, true);
            return true;
        default:
            return false;    
        }
    }
    
    /**
     * Sends a message to a specified socket if the specified socket exists.
     * @param socket_ID The ID of the socket to send the message to.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send(Integer socket_ID, byte[] message) {
        SocketData socketData = connections.get(socket_ID);
        if(socketData.socket != null) {
            try {
                socketData.jockey.send(message);
            } catch (IOException e) {
                removeSocket(socket_ID, false);
            }
        }
    }
    
    /**
     * Sends the given message to every connected socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void sendToAll(byte[] message) {
        Iterator<Integer> socket_ID_iterator = connections.keySet().iterator();
        while(socket_ID_iterator.hasNext()) {
            send(socket_ID_iterator.next(), message);
        }
    }
    
    public synchronized void disconnect(Integer socketID) {
        removeSocket(socketID, false);
    }
    
    public synchronized void disconnectAll() {
        // We are shutting down the client handler,
        // close and remove all connections
        Integer[] removeIDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < removeIDs.length; i++) {
            removeSocket(removeIDs[i], false);
        }
    }
    
    private synchronized void add_socket(Socket socket) throws IOException {
        SocketData socketData = new SocketData();
        socketData.socket = socket;
        socketData.jockey = new SocketBitJockey(socket.getInputStream(), socket.getOutputStream());
        socketData.timeoutManager = new TimeoutManager(6000, 10000);
        connections.put(nextConnectionID, socketData);
        newConnections.add(nextConnectionID);
        
        System.out.println("New socket opened: " + nextConnectionID);
        nextConnectionID++;
    }
    
    /**
     * Disconnects and removes a socket from the ClientHandler.
     * @param socket_ID The ID of the socket to be removed.
     */
    private synchronized void removeSocket(Integer socketID, boolean expected) {
        SocketData socketData = connections.get(socketID);
        if(socketData != null) {
            try {
                if(!expected) {
                    byte[] dc = {(byte)'D'};
                    socketData.jockey.send(dc);
                }
                socketData.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connections.remove(socketID);
            
            if(expected) {
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
