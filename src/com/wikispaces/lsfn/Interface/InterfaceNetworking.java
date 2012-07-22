package com.wikispaces.lsfn.Interface;

import java.io.IOException;
import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.SocketListener;
import com.wikispaces.lsfn.Shared.LSFN.SI;
import com.wikispaces.lsfn.Shared.LSFN.IS;

public class InterfaceNetworking {
    private SocketListener client;
    
    InterfaceNetworking() {
        client = new SocketListener();
    }
    
    public void connect(String host, int port) throws IOException {
        if(!client.isConnected()) client.connect(host, port);
    }
    
    public SI[] receive() throws IOException {
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
    
    public void send(IS message) throws IOException {
        if(client.isConnected()) client.send(message.toByteArray());
    }
    
    public boolean isConnected() {
        return client.isConnected();
    }
    
    public void close() {
        if(client.isConnected()) client.close();
    }
}
