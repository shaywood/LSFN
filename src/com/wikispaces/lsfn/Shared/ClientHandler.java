package com.wikispaces.lsfn.Shared;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends ServerSocket implements Runnable {
    private class SocketData {
        public Socket socket;
        public byte[] message_bytes;
        public byte[] length_bytes;
        public int bytes_read;
        public int message_size;
    }
    
    private HashMap<Integer, SocketData> connections;
    private int next_connection_ID;
    private List<Integer> new_connections;
    private List<Integer> new_disconnections;
    
    private static final int default_port = 14612;
    
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    public ClientHandler() throws IOException {
        super(default_port);
        connections = new HashMap<Integer, SocketData>();
        next_connection_ID = 0;
        new_connections = new ArrayList<Integer>();
        new_disconnections = new ArrayList<Integer>();
    }
    
    /**
     * This class accepts all incoming connections from InterfaceClients.
     * @param port The port to listen for clients on.
     * @throws IOException
     */
    public ClientHandler(int port) throws IOException {
        super(port);
        connections = new HashMap<Integer, SocketData>();
        next_connection_ID = 0;
        new_connections = new ArrayList<Integer>();
        new_disconnections = new ArrayList<Integer>();
    }
    
    /**
     * As described by Runnable.
     */
    public void run() {
        while (!this.isClosed()) {
            try {
                // We don't need a sleep() because this is blocking.
                // This thread basically accepts all incoming connections.
                Socket incoming_connection = this.accept();
                if(!this.isClosed()) {
                    add_socket(incoming_connection);
                }
            } catch (SocketException e) {
                if(!this.isClosed()) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets all of the messages that have been received from all connected clients.
     * @return The map key is the socket ID, the value is an array of messages. If a socket ID has no map entry, then no messages have been received from this client since the last read_all()
     */
    public HashMap<Integer, byte[][]> read_all() {
        Iterator<Integer> connection_iterator = connections.keySet().iterator();
        HashMap<Integer, byte[][]> messages = new HashMap<Integer, byte[][]>();
        while(connection_iterator.hasNext()) {
            Integer current_socket_ID = connection_iterator.next();
            SocketData socket_data = connections.get(current_socket_ID);
            if(socket_data.socket != null && socket_data.socket.isConnected() && !socket_data.socket.isClosed()) {
                byte[][] message_array = receive(current_socket_ID);
                if(message_array != null) {
                    messages.put(current_socket_ID, message_array);
                }
            } else {
                remove_socket(current_socket_ID);
            }
        }
        return messages;
    }
    
    private byte[][] receive(Integer socket_ID) {
        SocketData socket_data = connections.get(socket_ID);
        if(socket_data == null || !socket_data.socket.isConnected() || socket_data.socket.isClosed()) {
            remove_socket(socket_ID);
            return null;
        }
        ArrayList<byte[]> message_list = new ArrayList<byte[]>();
        
        try {
            while(socket_data.socket.getInputStream().available() > 0) {
                if(socket_data.bytes_read < 0) {
                    // We need to read a new message size from the input first
                    // This is 32 bits / 4 bytes long
                    socket_data.bytes_read += socket_data.socket.getInputStream().read(socket_data.length_bytes, 4 + socket_data.bytes_read, -socket_data.bytes_read);
                    if(socket_data.bytes_read == 0) {
                        for(int i = 0; i < 4; i++) {
                            socket_data.message_size = (socket_data.message_size << 8) + socket_data.length_bytes[i];
                        }
                        socket_data.message_bytes = new byte[socket_data.message_size];
                    }
                } else {
                    socket_data.bytes_read += socket_data.socket.getInputStream().read(socket_data.message_bytes, socket_data.bytes_read, socket_data.message_size - socket_data.bytes_read);
                    if(socket_data.bytes_read == socket_data.message_size) {
                        message_list.add(socket_data.message_bytes);
                        socket_data.message_bytes = null;
                        socket_data.bytes_read = -4;
                        socket_data.message_size = 0;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            remove_socket(socket_ID);
            return null;
        }
        
        if(message_list.size() == 0) return null;
        
        return message_list.toArray(new byte[0][]);
    }
    
    /**
     * Sends a message to a specified socket if the specified socket exists.
     * @param socket_ID The ID of the socket to send the message to.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send(Integer socket_ID, byte[] message) {
        SocketData socket_data = connections.get(socket_ID);
        if(socket_data.socket != null) {
            if(socket_data.socket.isConnected()) {
                try {
                    socket_data.socket.getOutputStream().write(message.length >> 24);
                    socket_data.socket.getOutputStream().write(message.length >> 16);
                    socket_data.socket.getOutputStream().write(message.length >> 8);
                    socket_data.socket.getOutputStream().write(message.length);
                    socket_data.socket.getOutputStream().write(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    remove_socket(socket_ID);
                }
            } else {
                remove_socket(socket_ID);
            }
        }
    }
    
    /**
     * Sends the given message to every connected socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send_to_all(byte[] message) {
        Iterator<Integer> socket_ID_iterator = connections.keySet().iterator();
        while(socket_ID_iterator.hasNext()) {
            send(socket_ID_iterator.next(), message);
        }
    }
    
    public synchronized void close_all() {
        // We are shutting down the client handler,
        // close and remove all connections
        Integer[] remove_IDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < remove_IDs.length; i++) {
            remove_socket(remove_IDs[i]);
        }
    }
    
    private synchronized void add_socket(Socket socket) throws IOException {
        SocketData socket_data = new SocketData();
        socket_data.socket = socket;
        socket_data.bytes_read = -4;
        socket_data.message_bytes = null;
        socket_data.message_size = 0;
        socket_data.length_bytes = new byte[4];
        connections.put(next_connection_ID, socket_data);
        new_connections.add(next_connection_ID);
        System.out.println("New socket opened: " + next_connection_ID);
        next_connection_ID++;
    }
    
    /**
     * Disconnects and removes a socket from the ClientHandler.
     * @param socket_ID The ID of the socket to be removed.
     */
    public synchronized void remove_socket(Integer socket_ID) {
        SocketData socket_data = connections.get(socket_ID);
        if(socket_data != null) {
            try {
                socket_data.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connections.remove(socket_ID);
            new_disconnections.add(socket_ID);
            System.out.println("Closed socket: " + socket_ID);
        }
    }
    
    public synchronized Integer[] get_new_connections() {
        Integer[] IDs = new_connections.toArray(new Integer[0]);
        new_connections.clear();
        return IDs;
    }
    
    public synchronized Integer[] get_new_disconnections() {
        Integer[] IDs = new_disconnections.toArray(new Integer[0]);
        new_disconnections.clear();
        return IDs;
    }
}
