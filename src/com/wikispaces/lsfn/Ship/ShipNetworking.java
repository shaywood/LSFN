package com.wikispaces.lsfn.Ship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.ClientHandler;
import com.wikispaces.lsfn.Shared.SocketListener;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SI;

public class ShipNetworking {
    private SocketListener client;
    private ClientHandler server;
    private Thread serverThread;
    
    ShipNetworking() {
        client = new SocketListener();
        server = new ClientHandler();
    }
    
    // ENV
    
    public void connectToENV(String host, int port) throws IOException {
        if(!client.isConnected()) client.connect(host, port);
    }
    
    public SI[] receiveFromENV() throws IOException {
        if(client.isConnected()) {
            byte[][] messages = client.receive();
            ArrayList<SI> messageList = new ArrayList<SI>();
            for(int i = 0; i < messages.length; i++) {
                try {
                    SI message = SI.parseFrom(messages[i]);
                    messageList.add(message);
                } catch (InvalidProtocolBufferException e) {
                    
                }
            }
            if(messageList.size() == 0) return null;
            return messageList.toArray(new SI[0]);
        } else {
            return null;
        }
    }
    
    public void sendToENV(IS message) throws IOException {
        if(client.isConnected()) client.send(message.toByteArray());
    }
    
    public boolean isConnectedtoENV() {
        return client.isConnected();
    }
    
    public void disconnectFromENV() {
        if(client.isConnected()) client.close();
    }
    
    // INT
    
    public void openINTServer() throws IOException {
        server.open();
        serverThread = new Thread(server);
    }
    
    public HashMap<Integer, IS[]> readAll() {
        HashMap<Integer, byte[][]> messages = server.readAll();
        if(messages == null) return null;
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
            if(messageList.size() == 0) return null;
            parsedMessages.put(socketID, messageList.toArray(new IS[0]));
        }
        
        if(parsedMessages.size() == 0) return null;
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
    
    public void closeINTServer() throws IOException {
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
