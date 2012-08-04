package com.wikispaces.lsfn.Ship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.ClientHandler;
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
    
    /**
     * Creates new server and client objects
     */
    ShipNetworking() {
        client = new SocketListener();
        server = new ClientHandler();
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
            if(messageList.size() != 0) parsedMessages.put(socketID, messageList.toArray(new IS[0]));
        }
        
        return parsedMessages;
    }
    
    public void sendToINT(Integer socketID, SI message) {
        server.send(socketID, message.toByteArray());
    }
    
    public void sendToAllINTs(SI message) {
        server.sendToAll( message.toByteArray());
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
    
    public Integer[] getNewINTConnections() {
        return server.getNewConnections();
    }
    
    public Integer[] getControlledINTDisconnections() {
        return server.getControlledDisconnections();
    }
    
    public Integer[] getUncontrolledINTDisconnections() {
        return server.getUncontrolledDisconnections();
    }
}
