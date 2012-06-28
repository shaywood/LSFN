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
                boolean more_bytes = true;
                while(more_bytes) {
                    if(bytes_read < 0) {
                        // We need to read a new message size from the input first
                        // This is 32 bits / 4 bytes long
                        bytes_read += this.getInputStream().read(length_bytes, 4 + bytes_read, -bytes_read);
                        if(bytes_read < 0) {
                            more_bytes = false;
                        } else {
                            for(int i = 0; i < 4; i++) {
                                message_size = (message_size << 8) + length_bytes[i];
                            }
                            message_bytes = new byte[message_size];
                        }
                    } else {
                        bytes_read += this.getInputStream().read(length_bytes, bytes_read, message_size - bytes_read);
                        if(bytes_read < message_size) {
                            more_bytes = false;
                        } else {
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
        byte[] to_send = new byte[4 + message.length];
        for(int i = 0; i < 4; i++) {
            to_send[i] = (byte)(message.length >> ((3 - i) * 8));
        }
        for(int i = 4; i < message.length + 4; i++) {
            to_send[i] = message[i - 4];
        }
        System.out.println(bytes_to_hex(to_send));
        if(message.length < Integer.MAX_VALUE) {
            try {
                this.getOutputStream().write(to_send);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private synchronized void add_message(byte[] message) {
        message_buffer.add(message);
    }
    
    public static String bytes_to_hex(byte[] bytes) {
        String byte_str = "";
        for(int i = 0; i < bytes.length; i++) {
            String hex_pair = Integer.toHexString(bytes[i]);
            if(hex_pair.length() == 1) hex_pair = "0" + hex_pair;
            byte_str += hex_pair;
        }
        return byte_str;
    }
}
