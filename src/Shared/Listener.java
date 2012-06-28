package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.util.*;
import java.net.*;

public class Listener extends Socket implements Runnable {
    private ArrayList<byte[]> message_buffer;
    private byte[] message_bytes;
    private byte[] length_bytes;
    private int bytes_read;
    private int message_size;
    
    /**
     * Creates a Listener that asynchronously collects input from a Socket .
     * @param host The host that the Socket will connect to.
     * @param port The port that the Socket will connect to.
     * @throws IOException
     */
    public Listener(String host, int port) throws IOException {
        super(host, port);
        message_buffer = new ArrayList<byte[]>();
        message_bytes = null;
        length_bytes = new byte[4];
        bytes_read = -4;
        message_size = 0;
    }
    
    /**
     * As described by Runnable.
     */
    public void run() {
        // Starts the listener
        // Should be on a new thread (because it implements Runnable)
        boolean running = true;
        while(running) {
            try {
                while(this.getInputStream().available() > 0) {
                    if(bytes_read < 0) {
                        // We need to read a new message size from the input first
                        // This is 32 bits / 4 bytes long
                        bytes_read += this.getInputStream().read(length_bytes, 4 + bytes_read, -bytes_read);
                        if(bytes_read == 0) {
                            for(int i = 0; i < 4; i++) {
                                message_size = (message_size << 8) + length_bytes[i];
                            }
                            message_bytes = new byte[message_size];
                        }
                    } else {
                        bytes_read += this.getInputStream().read(message_bytes, bytes_read, message_size - bytes_read);
                        if(bytes_read == message_size) {
                            add_message(message_bytes);
                            message_bytes = null;
                            bytes_read = -4;
                            message_size = 0;
                        }
                    }
                }
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
    
    /**
     * Gets an array of messages that have been received from the socket in the order they were received.
     * @return Messages received in order.
     */
    public synchronized byte[][] get_messages() {
        byte[][] messages = message_buffer.toArray(new byte[0][]);
        message_buffer.clear();
        return messages;
    }
    
    /**
     * Sends the given message through the socket.
     * @param message The message to be sent.
     */
    public void send(byte[] message) {
        if(message.length < Integer.MAX_VALUE) {
            try {
                this.getOutputStream().write(message.length >> 24);
                this.getOutputStream().write(message.length >> 16);
                this.getOutputStream().write(message.length >> 8);
                this.getOutputStream().write(message.length);
                this.getOutputStream().write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private synchronized void add_message(byte[] message) {
        message_buffer.add(message);
    }
    
    public static String bytes_to_hex(byte[] bytes) {
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
