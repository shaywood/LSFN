package com.wikispaces.lsfn.Ship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.ClientHandler;
import com.wikispaces.lsfn.Shared.LSFN;
import com.wikispaces.lsfn.Shared.SocketListener;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import com.wikispaces.lsfn.Shared.LSFN.ES;
import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;

public class ShipNetworking {
    private SocketListener client;
    private ClientHandler server;
    private Thread serverThread;
    HashSet<Integer> verifiedConnections;
    HashSet<Integer> brokenConnections;
    HashMap<Integer, Long> INTConnectionTime;
    Integer newConnectionTimeout;
    
    /**
     * Creates new server and client objects
     */
    ShipNetworking(Integer newConnectionTimeout) {
        client = new SocketListener();
        server = new ClientHandler();
        verifiedConnections = new HashSet<Integer>();
        this.INTConnectionTime = new HashMap<Integer, Long>();
        this.newConnectionTimeout = newConnectionTimeout;
    }
    
    // ENV
    
    public ConnectionStatus connectToENV(String host, int port) {
        try {
            client.connect(host, port);
        } catch (IOException e) {
        }
        return client.getConnectionStatus();
    }
    
    public ES[] receiveFromENV() {
        if(client.getConnectionStatus() == ConnectionStatus.CONNECTED) {
            byte[][] messages;
            try {
                messages = client.receive();
            } catch (IOException e1) {
                return null;
            }
            
            ArrayList<ES> messageList = new ArrayList<ES>();
            for(int i = 0; i < messages.length; i++) {
                try {
                    ES message = ES.parseFrom(messages[i]);
                    messageList.add(message);
                } catch (InvalidProtocolBufferException e) {
                    
                }
            }
            return messageList.toArray(new ES[0]);
        } else {
            return null;
        }
    }
    
    public ConnectionStatus sendToENV(SE message) {
        try {
            client.send(message.toByteArray());
        } catch (IOException e) {
        }
        return client.getConnectionStatus();
    }
    
    public ConnectionStatus isConnectedToENV() {
        return client.getConnectionStatus();
    }
    
    public ConnectionStatus disconnectFromENV() {
        client.close();
        return client.getConnectionStatus();
    }
    
    // INT
    
    public boolean openINTServer() {
        try {
            server.open(14612);
        } catch (IOException e) {
            return false;
        }
        serverThread = new Thread(server);
        serverThread.start();
        return true;
    }
    
    public HashMap<Integer, IS[]> readAllFromINTs() {
        HashMap<Integer, byte[][]> messages = server.readAll();
        HashMap<Integer, IS[]> parsedMessages = new HashMap<Integer, IS[]>();
        
        for(Integer socketID : messages.keySet()) {
            byte[][] messageArray = messages.get(socketID);
            ArrayList<IS> messageList = new ArrayList<IS>();
            for(int i = 0; i < messageArray.length; i++) {
                try {
                    IS message = IS.parseFrom(messageArray[i]);
                    messageList.add(message);
                } catch (InvalidProtocolBufferException e) {
                    
                }
            }
            
            IS[] parsedMessageArray = messageList.toArray(new IS[0]);
            if(parsedMessageArray.length != 0) {
                if(verifiedConnections.contains(socketID)) {
                    parsedMessages.put(socketID, messageList.toArray(new IS[0]));
                } else {
                    if(parsedMessageArray[0].hasHandshake()) {
                        handleHandshake(socketID, parsedMessageArray[0].getHandshake());
                    } else {
                        server.disconnect(socketID);
                    }
                }
            }
        }
        
        return parsedMessages;
    }
    
    public void sendToINT(Integer socketID, SI message) {
        server.send(socketID, message.toByteArray());
    }
    
    public void sendToAllINTs(SI message) {
        server.sendToAll(message.toByteArray());
    }
    
    public void disconnectINT(Integer socketID) {
        server.disconnect(socketID);
    }
    
    public void disconnectAllINTs() {
        server.disconnectAll();
    }
    
    public boolean isINTServerOpen() {
        return server.isOpen();
    }
    
    public void closeINTServer() {
        server.close();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            // Again, we don't really care as we're disposing of it anyway
        }
        server = null;
        serverThread = null;
    }
    
    // INTManager
    
    public void handleConnectionUpdates() {
        handleNewConnections();
        timeoutSilentNewConnections();
        handleGoodDisconnections();
        handleBadDisconnections();
    }
    
    private void handleNewConnections() {
        Integer[] INTIDs = server.getNewConnections();
        for(int i = 0; i < INTIDs.length; i++) {
            INTConnectionTime.put(i, Calendar.getInstance().getTimeInMillis());
        }
    }
    
    private void timeoutSilentNewConnections() {
        for(Integer i : INTConnectionTime.keySet()) {
            if(Calendar.getInstance().getTimeInMillis() - INTConnectionTime.get(i) > newConnectionTimeout) {
                server.disconnect(i);
                INTConnectionTime.remove(i);
            }
        }
    }
    
    private void handleGoodDisconnections() {
        Integer[] INTIDs = server.getControlledDisconnections();
        for(int i = 0; i < INTIDs.length; i++) {
            INTConnectionTime.remove(INTIDs[i]);
            verifiedConnections.remove(INTIDs[i]);
            brokenConnections.remove(INTIDs[i]);
        }
    }
    
    private void handleBadDisconnections() {
        Integer[] INTIDs = server.getUncontrolledDisconnections();
        for(int i = 0; i < INTIDs.length; i++) {
            INTConnectionTime.remove(INTIDs[i]);
            if(verifiedConnections.contains(INTIDs[i])) {
                verifiedConnections.remove(INTIDs[i]);
                brokenConnections.add(INTIDs[i]);
            }
        }
    }
    
    private void handleHandshake(Integer INTID, LSFN.IS.Handshake handshake) {
        if(handshake == LSFN.IS.Handshake.HELLO) {
            SI handshakeOut = SI.newBuilder()
                    .setHandshake(SI.Handshake.newBuilder()
                            .setType(SI.Handshake.Type.HELLO)
                            .setIntID(INTID)
                            .build())
                    .build();
            server.send(INTID, handshakeOut.toByteArray());
            INTConnectionTime.remove(INTID);
            verifiedConnections.add(INTID);
            System.out.println("New INT connected with ID " + INTID + ".");
        }
    }

    /**
     * Used to retrieve the current set of verified connections
     * Must not be modified
     * @return
     */
    public HashSet<Integer> getVerifiedConnections() {
        return verifiedConnections;
    }
    
    /**
     * Used to retrieve the current set of broken connections
     * Must not be modified
     * @return
     */
    public HashSet<Integer> getBrokenConnections() {
        return brokenConnections;
    }
}
