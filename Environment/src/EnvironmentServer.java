import java.io.*;
import java.util.*;


public class EnvironmentServer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
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
                if(message.equals("Stop server")) {
                    stop = true;
                }
            }
            if(stop) break;
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        clientHandler.close();
    }

    public static void failAndExit(String str) {
        System.err.println(str);
        System.exit(1);
    }
}
