import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private ServerSocket server;
    private HashMap<Integer, Socket> connections;
    private HashMap<Integer, BufferedReader> readers;
    private HashMap<Integer, PrintWriter> writers;
    private int next_connection_ID;
    private Object lock;
    
    private static final int default_port = 14612;
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    ClientHandler() throws IOException {
        server = new ServerSocket(default_port);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
        lock = new Object();
    }
    
    /**
     * This class accepts all incoming connections from InterfaceClients.
     * @param port The port to listen for clients on
     * @throws IOException
     */
    ClientHandler(int port) throws IOException {
        server = new ServerSocket(port);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
        lock = new Object();
    }
    
    public void run() {
        while (!server.isClosed()) {
            try {
                Socket incoming_connection = server.accept();
                synchronized(lock) {
                    connections.put(next_connection_ID, incoming_connection);
                    readers.put(next_connection_ID, new BufferedReader(new InputStreamReader(incoming_connection.getInputStream())));
                    writers.put(next_connection_ID, new PrintWriter(incoming_connection.getOutputStream(), true));
                    next_connection_ID++;
                }
            } catch (SocketException e) {
                if(!server.isClosed()) {
                    System.err.println("Could not accept the socket (socket).");
                    System.err.println(e.getLocalizedMessage());
                }
            } catch (IOException e) {
                System.err.println("Could not accept the socket (IO).");
                System.err.println(e.getLocalizedMessage());
            } catch (SecurityException e) {
                System.err.println("Security error.");
                System.err.println(e.getLocalizedMessage());
            } catch (Exception e) {
                System.err.println("Some other error occured.");
                System.err.println(e.getLocalizedMessage());
            }
        }
        
        Iterator<Integer> connection_iterator = connections.keySet().iterator();
        while(connection_iterator.hasNext()) {
            Integer current_socket_id = connection_iterator.next();
            Socket current_socket = connections.get(current_socket_id);
            try {
                current_socket.close();
                connections.remove(current_socket_id);
            } catch (IOException e) {
                System.err.println("Failed to close socket.");
                System.err.println(e.getLocalizedMessage());
            }
        }
    }
    
    public HashMap<Integer, String> read_all() {
        Iterator<Integer> connection_iterator = connections.keySet().iterator();
        HashMap<Integer, String> messages = new HashMap<Integer, String>();
        while(connection_iterator.hasNext()) {
            
            Integer current_socket_id = connection_iterator.next();
            Socket current_socket = connections.get(current_socket_id);
            if(current_socket.isConnected()) {
                BufferedReader current_reader = readers.get(current_socket_id);
                try {
                    if(current_reader != null && current_reader.ready()) {
                        messages.put(current_socket_id, current_reader.readLine());
                    }
                } catch (IOException e) {
                    System.err.println("Could not read socket.");
                    System.err.println(e.getLocalizedMessage());
                }
            } else {
                try {
                    System.out.println("Closing socket " + current_socket_id);
                    current_socket.close();
                    connections.remove(current_socket_id);
                } catch (IOException e) {
                    System.err.println("Failed to close socket.");
                    System.err.println(e.getLocalizedMessage());
                }
            }
        }
        return messages;
    }
    
    public void send(Integer socket_id, String message) {
        PrintWriter writer = writers.get(socket_id);
        if(writer != null) {
            System.out.println("Sending message.");
            writer.println(message);
        } else {
            System.out.println("Writer " + socket_id + " is null.");
        }
    }
    
    public void close() {
        try {
            server.close();
        } catch (IOException e) {
            System.err.println("Failed to close ServerSocket.");
            System.err.println(e.getLocalizedMessage());
        }
    }
}
