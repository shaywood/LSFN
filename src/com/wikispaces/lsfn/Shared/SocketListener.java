package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.util.*;
import java.net.*;

public class SocketListener {
    private Socket socket;
    private SocketBitJockey jockey;
    private TimeoutManager timeoutManager;
    
    public enum ConnectionStatus {
        NEVER_CONNECTED,
        CONNECTED,
        DISCONNECTED_CLEAN,
        DISCONNECTED_UNCLEAN
    }
    private ConnectionStatus connectionStatus;
    
    /**
     * Creates a Listener that asynchronously collects input from a socket.
     * @param host The host that the Socket will connect to.
     * @param port The port that the Socket will connect to.
     * @throws IOException
     */
    public SocketListener() {
        socket = null;
        jockey = null;
        timeoutManager = new TimeoutManager(6000, 10000);
        
        connectionStatus = ConnectionStatus.NEVER_CONNECTED;
    }
    
    public SocketListener(Socket socket) throws IOException {
        this.socket = socket;
        jockey = new SocketBitJockey(socket.getInputStream(), socket.getOutputStream());
        timeoutManager = new TimeoutManager(6000, 10000);
        
        connectionStatus = ConnectionStatus.CONNECTED;
    }
    
    public void connect(String host, int port) throws IOException {
        if(connectionStatus != ConnectionStatus.CONNECTED) {
            socket = new Socket(host, port);
            jockey = new SocketBitJockey(socket.getInputStream(), socket.getOutputStream());
            timeoutManager = new TimeoutManager(6000, 10000);
            connectionStatus = ConnectionStatus.CONNECTED;
        } else {
            throw new IOException("Socket is already connected.");
        }
    }
    
    public byte[][] receive() throws IOException {
        checkTimeouts();
        if(connectionStatus == ConnectionStatus.CONNECTED) {
            try {
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
            } catch (IOException e) {
                // If we can't read the messages, we'll close the socket.
                sendDC();
                closeSocket();
                connectionStatus = ConnectionStatus.DISCONNECTED_UNCLEAN;
                throw new IOException("Socket is not connected.");
            }
        } else {
            throw new IOException("Socket is not connected.");
        }
    }
    
    public void send(byte[] message) throws IOException {
        checkTimeouts();
        if(connectionStatus == ConnectionStatus.CONNECTED) {
            try {
                jockey.send(message);
                timeoutManager.sendOccured();
            } catch (IOException e) {
                // If we can't read the messages, we'll close the socket.
                sendDC();
                closeSocket();
                connectionStatus = ConnectionStatus.DISCONNECTED_UNCLEAN;
                throw new IOException("Socket is not connected.");
            }
        }
    }
    
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
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
            closeSocket();
            connectionStatus = ConnectionStatus.DISCONNECTED_CLEAN;
            return true;
        default:
            return false;    
        }
    }

    private void checkTimeouts() throws IOException {
        if(connectionStatus == ConnectionStatus.CONNECTED) {
            if(timeoutManager.shouldDeclareAlive()) {
                byte[] ping = {(byte)'P'};
                jockey.send(ping);
            }
            
            if(timeoutManager.shouldTimeout()) {
                sendDC();
                closeSocket();
                connectionStatus = ConnectionStatus.DISCONNECTED_UNCLEAN;
                throw new IOException("Connection timeout.");
            }
        }
    }
    
    private void sendDC() {
        try {
            byte[] dc = {(byte)'D'};
            jockey.send(dc);
        } catch (IOException e) {
            // We just don't care anymore.
        }
    }
    
    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // We just don't care anymore.
        }
        socket = null;
        jockey = null;
    }
    
    public void close() {
        if(connectionStatus == ConnectionStatus.CONNECTED) {
            sendDC();
            closeSocket();
            connectionStatus = ConnectionStatus.DISCONNECTED_CLEAN;
        }
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
