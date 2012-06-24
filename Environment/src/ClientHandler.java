import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends ServerSocket implements Runnable {
    private HashMap<Integer, Socket> connections;
    private HashMap<Integer, BufferedReader> readers;
    private HashMap<Integer, PrintWriter> writers;
    private int next_connection_ID;
    
    private static final int default_port = 14612;
    /**
     * This class accepts all incoming connections from InterfaceClients on the default port.
     * @throws IOException
     */
    ClientHandler() throws IOException {
        super(default_port);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
    }
    
    /**
     * This class accepts all incoming connections from InterfaceClients.
     * @param port The port to listen for clients on
     * @throws IOException
     */
    ClientHandler(int port) throws IOException {
        super(port);
        connections = new HashMap<Integer, Socket>();
        readers = new HashMap<Integer, BufferedReader>();
        writers = new HashMap<Integer, PrintWriter>();
        next_connection_ID = 0;
    }
    
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
                if(this.isClosed()) {
                    shut_down();                    
                } else {
                    // TODO
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }
    
    public HashMap<Integer, String> read_all() {
        Iterator<Integer> connection_iterator = connections.keySet().iterator();
        HashMap<Integer, String> messages = new HashMap<Integer, String>();
        while(connection_iterator.hasNext()) {
            Integer current_socket_ID = connection_iterator.next();
            Socket current_socket = connections.get(current_socket_ID);
            if(current_socket.isConnected() && !current_socket.isClosed()) {
                String message = receive(current_socket_ID);
                if(message != null) {
                    messages.put(current_socket_ID, message);
                }
            } else {
                remove_socket(current_socket_ID);
            }
        }
        return messages;
    }
    
    public String receive(Integer socket_ID) {
        Socket socket = connections.get(socket_ID);
        if(socket == null || !socket.isConnected() || socket.isClosed()) {
            remove_socket(socket_ID);
            return null;
        }
        BufferedReader current_reader = readers.get(socket_ID);
        try {
            if(!current_reader.ready()) {
                return null;
            }
        } catch (IOException e1) {
            // TODO
            e1.printStackTrace();
            remove_socket(socket_ID);
            return null;
        }
        
        String message = null;
        try {
            message = current_reader.readLine();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        if(message == null) {
            remove_socket(socket_ID);
        }
        
        return message;
    }
    
    public void send(Integer socket_ID, String message) {
        Socket socket = connections.get(socket_ID);
        if(socket.isConnected()) {
            writers.get(socket_ID).println(message);
        } else {
            remove_socket(socket_ID);
        }
    }
    
    public void send_to_all(String message) {
        Iterator<Integer> writer_iterator = writers.keySet().iterator();
        while(writer_iterator.hasNext()) {
            Integer current_socket_ID = writer_iterator.next();
            writers.get(current_socket_ID).println(message);
        }
    }
    
    private synchronized void shut_down() {
        // We are shutting down the client handler,
        // close and remove all connections
        Integer[] remove_IDs = connections.keySet().toArray(new Integer[0]);
        for(int i = 0; i < remove_IDs.length; i++) {
            remove_socket(remove_IDs[i]);
        }
    }
    
    private synchronized void add_socket(Socket socket) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        connections.put(next_connection_ID, socket);
        readers.put(next_connection_ID, br);
        writers.put(next_connection_ID, pw);
        System.out.println("New socket opened: " + next_connection_ID);
        next_connection_ID++;
    }
    
    public synchronized void remove_socket(Integer socket_ID) {
        Socket socket = connections.get(socket_ID);
        if(socket != null) {
            try {
                connections.get(socket_ID).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connections.remove(socket_ID);
            readers.remove(socket_ID);
            writers.remove(socket_ID);
            System.out.println("Closed socket: " + socket_ID);
        }
    }
}
