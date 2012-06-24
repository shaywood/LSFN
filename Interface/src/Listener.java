import java.io.*;
import java.util.*;
import java.net.*;

public class Listener extends Socket implements Runnable {
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<String> message_buffer;
    
    Listener(String host, int port) throws IOException {
        super(host, port);
        input = new BufferedReader(new InputStreamReader(this.getInputStream()));
        output = new PrintWriter(this.getOutputStream(), true);
        message_buffer = new ArrayList<String>();
    }
    
    public void run() {
        // Starts the listener
        // Should be on a new thread (because it implements Runnable)
        boolean running = true;
        while(running) {
            try {
                if(input.ready()) {
                    add_message(input.readLine());
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
    
    public synchronized String[] get_messages() {
        String[] strings = message_buffer.toArray(new String[0]);
        message_buffer.clear();
        return strings;
    }
    
    public void send(String message) {
        output.println(message);
    }
    
    private synchronized void add_message(String message) {
        message_buffer.add(message);
    }
}
