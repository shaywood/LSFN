package com.wikispaces.lsfn.Interface;

import java.io.IOException;
import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wikispaces.lsfn.Shared.ServerListener;
import com.wikispaces.lsfn.Shared.LSFN.SI;

public class InterfaceNetworking {
    private ServerListener client;
    
    InterfaceNetworking() {
        client = new ServerListener();
    }
    
    public void connect(String host, int port) throws IOException {
        client.connect(host, port);
    }
    
    public SI[] receive() throws IOException {
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
    }
    
    public void send(SI message) throws IOException {
        client.send(message.toByteArray());
    }
    
    public void close() {
        client.close();
    }
}
