package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.util.*;
import java.net.*;

public class SocketListener {
    private Socket socket;
    private SocketBitJockey jockey;
    private TimeoutManager timeoutManager;
    
    private boolean connected;
    private boolean cleanDisconnect;
    
    /**
     * Creates a Listener that asynchronously collects input from a declareAliveIntervalSocket .
     * @param host The host that the Socket will connect to.
     * @param port The port that the Socket will connect to.
     * @throws IOException
     */
    public SocketListener() {
        socket = null;
        jockey = null;
        timeoutManager = new TimeoutManager(6000, 10000);
        
        connected = false;
        cleanDisconnect = false;
    }
    
    public SocketListener(Socket socket) throws IOException {
        this.socket = socket;
        jockey = new SocketBitJockey(socket.getInputStream(), socket.getOutputStream());
        timeoutManager = new TimeoutManager(6000, 10000);
        
        connected = true;
        cleanDisconnect = false;
    }
    
    public void connect(String host, int port) throws IOException {
        if(socket == null) {
            socket = new Socket(host, port);
            jockey = new SocketBitJockey(socket.getInputStream(), socket.getOutputStream());
            timeoutManager = new TimeoutManager(6000, 10000);
            connected = true;
            cleanDisconnect = false;
        }
    }
    
    public byte[][] receive() throws IOException {
        checkTimeouts();
        if(connected) {
            byte[][] messages = jockey.readMessages();
            ArrayList<byte[]> messageList = new ArrayList<byte[]>();
            if(messages.length > 0) {
                timeoutManager.receiveOccured();
                for(int i = 0; i < messages.length; i++) {
                    if(!checkMessageForSignal(messages[i])) {
                        messageList.add(messages[i]);
                    }
                }
            }
            return messageList.toArray(new byte[0][]);
        } else {
            return null;
        }
    }
    
    public void send(byte[] message) throws IOException {
        checkTimeouts();
        if(connected) {
            jockey.send(message);
            timeoutManager.sendOccured();
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public boolean wasCleanDisconnect() {
        return cleanDisconnect;
    }
    
    private boolean checkMessageForSignal(byte[] signalBytes) {
        if(signalBytes.length != 1) return false;
        switch ((char)signalBytes[0]) {
        case 'P':
            // The pong signal has been received
            // We've actually already done what we needed (resetting the receive counter)
            return true;
        case 'D':
            // The disconnect signal has been received
            connected = false;
            cleanDisconnect = true;
            close();
            return true;
        default:
            return false;    
        }
    }

    private void checkTimeouts() throws IOException {
        if(socket != null) {
            if(timeoutManager.shouldDeclareAlive()) {
                byte[] ping = {(byte)'P'};
                jockey.send(ping);
            }
            
            if(timeoutManager.shouldTimeout()) {
                byte[] dc = {(byte)'D'};
                jockey.send(dc);
                throw new IOException("Connection timeout.");
            }
        }
    }
    
    public void close() {
        if(connected) {
            byte[] dc = {(byte)'D'};
            try {
                jockey.send(dc);
            } catch (IOException e) {
                // We just don't care anymore.
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            // We just don't care anymore.
        }
        
        socket = null;
        jockey = null;
    }
    
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
