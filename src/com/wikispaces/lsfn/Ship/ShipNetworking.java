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
    
    /**
     * Connects to the ENV on the give host and port if not already connected.
     * @param host The host to connect to.
     * @param port The port to connect on.
     * @throws IOException
     */
    public void connectToENV(String host, int port) throws IOException {
        if(!client.isConnected()) client.connect(host, port);
    }
    
    /**
     * 
     * @return
     * @throws IOException
     */
    public ES[] receiveFromENV() throws IOException {
        if(client.isConnected()) {
            byte[][] messages = client.receive();
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
            throw new IOException("Not connected to ENV");
        }
    }
    
    public void sendToENV(SE message) throws IOException {
        if(client.isConnected()) {
            client.send(message.toByteArray());
        } else {
            throw new IOException("Not connected to ENV");
        }
    }
    
    public boolean isConnectedtoENV() {
        return client.isConnected();
    }
    
    public void disconnectFromENV() {
        if(client.isConnected()) client.close();
    }
    
    // INT
    
    public void openINTServer() throws IOException {
        server.open(14612);
        serverThread = new Thread(server);
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
