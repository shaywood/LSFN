package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.util.ArrayList;

public class SocketBitJockey {
    private InputStream input;
    private OutputStream output;
    
    private byte[] messageBytes;
    private byte[] lengthBytes;
    private int bytesRead;
    private int messageSize;
    
    private ArrayList<byte[]> completedMessages;
    
    SocketBitJockey(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
        
        this.bytesRead = -4;
        this.lengthBytes = new byte[4];
    }
    
    public byte[][] readMessages() throws IOException {
        while(input.available() > 0) {
            if(bytesRead < 0) {
                // We need to read a new message size from the input first
                // This is 32 bits / 4 bytes long
                bytesRead += input.read(lengthBytes, 4 + bytesRead, -bytesRead);
                if(bytesRead == 0) {
                    for(int i = 0; i < 4; i++) {
                        messageSize = (messageSize << 8) + lengthBytes[i];
                    }
                    messageBytes = new byte[messageSize];
                }
            } else {
                bytesRead += input.read(messageBytes, bytesRead, messageSize - bytesRead);
                if(bytesRead == messageSize) {
                    completedMessages.add(messageBytes);
                    messageBytes = null;
                    bytesRead = -4;
                    messageSize = 0;
                }
            }
        }
        
        byte[][] messages = {};
        if(completedMessages.size() > 0) {
            messages = completedMessages.toArray(new byte[0][]);
            completedMessages.clear();
        }
        return messages;
    }
    
    public void send(byte[] message) throws IOException {
        if(message.length < Integer.MAX_VALUE) {
            output.write(message.length >> 24);
            output.write(message.length >> 16);
            output.write(message.length >> 8);
            output.write(message.length);
            output.write(message);
        }
    }
}
