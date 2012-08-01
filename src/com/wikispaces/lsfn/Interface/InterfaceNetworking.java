package com.wikispaces.lsfn.Interface;

import java.io.IOException;
import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.SocketListener;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.SocketListener.ConnectionStatus;

public class InterfaceNetworking {
    private SocketListener client;
    
    InterfaceNetworking() {
        client = new SocketListener();
    }
    
    public ConnectionStatus connectToSHIP(String host, int port) {
        try {
            client.connect(host, port);
        } catch (IOException e) {
        }
        return client.getConnectionStatus();
    }
    
    public SI[] receiveFromSHIP() {
        if(client.getConnectionStatus() == ConnectionStatus.CONNECTED) {
            byte[][] messages;
            try {
                messages = client.receive();
            } catch (IOException e1) {
                return null;
            }
            
            ArrayList<SI> messageList = new ArrayList<SI>();
            for(int i = 0; i < messages.length; i++) {
                try {
                    SI message = SI.parseFrom(messages[i]);
                    messageList.add(message);
                } catch (InvalidProtocolBufferException e) {
                    
                }
            }
            return messageList.toArray(new SI[0]);
        } else {
            return null;
        }
    }
    
    public ConnectionStatus sendToSHIP(IS message) {
        try {
            client.send(message.toByteArray());
        } catch (IOException e) {
        }
        return client.getConnectionStatus();
    }
    
    public ConnectionStatus isConnectedToSHIP() {
        return client.getConnectionStatus();
    }
    
    public ConnectionStatus disconnectFromSHIP() {
        client.close();
        return client.getConnectionStatus();
    }
}
