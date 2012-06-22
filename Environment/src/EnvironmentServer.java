import java.io.*;
import java.util.*;


public class EnvironmentServer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        /*ServerSocket socket_server = null;
        try {
            socket_server = new ServerSocket(14612); 
        } catch (IOException e) {
            failAndExit("Could not open on port 14612: " + e.getLocalizedMessage());
        }
        
        System.out.println("ServerSocket open.");
        
        Socket client_socket = null;
        try {
            client_socket = socket_server.accept();
        } catch (IOException e) {
            failAndExit("Failed to accept connection: " + e.getLocalizedMessage());
        }
        
        System.out.println("Accepted connection.");
        
        PrintWriter to_client = null;
        BufferedReader from_client = null;
        try {
            to_client = new PrintWriter(client_socket.getOutputStream(), true);
            from_client = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
        } catch (IOException e) {
            failAndExit("Failed to create connection reader / writer: " + e.getLocalizedMessage());
        }
            
        String inputLine = "";
        
        while(true) {
            try {
                inputLine = from_client.readLine();
            } catch (IOException e) {
                failAndExit("Error in reading from client: " + e.getLocalizedMessage());
            }
            
            System.out.println("Echoing: " + inputLine);
            to_client.println(inputLine);
            
            if (inputLine.equals("Bye.")) {
                break;
            }
        }
        
        System.out.println("Closing connections.");
        
        try {
            to_client.close();
            from_client.close();
            client_socket.close();
            socket_server.close();
        } catch (IOException e) {
            failAndExit("Failed to close buffers / connections: " + e.getLocalizedMessage());                
        }*/
        ClientHandler clientHandler = null;
        try {
            clientHandler = new ClientHandler();
        } catch (IOException e) {
            failAndExit(e.getLocalizedMessage());
        }
        
        Thread client_thread = new Thread(clientHandler);
        client_thread.start();
        
        while(true) {
            HashMap<Integer, String> messages = clientHandler.read_all();
            Iterator<Integer> message_iterator = messages.keySet().iterator();
            boolean stop = false;
            while(message_iterator.hasNext()) {
                Integer message_ID = message_iterator.next();
                String message = messages.get(message_ID);
                System.out.println("Socket " + message_ID + " sent: \"" + message + "\" length " + message.length());
                clientHandler.send(message_ID, message);
                System.out.println("Sent message.");
                if(message.equals("Stop server")) {
                    stop = true;
                }
            }
            if(stop) break;
        }
        
        clientHandler.close();
        try {
            client_thread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void failAndExit(String str) {
        System.err.println(str);
        System.exit(1);
    }
}
