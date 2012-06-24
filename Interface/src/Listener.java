import java.io.*;
import java.util.*;
import java.net.*;

public class Listener extends Socket implements Runnable {
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<String> message_buffer;
    
    /**
     * Creates a Listener that asynchronously collects input from a Socket .
     * @param host The host that the Socket will connect to.
     * @param port The port that the Socket will connect to.
     * @throws IOException
     */
    Listener(String host, int port) throws IOException {
        super(host, port);
        input = new BufferedReader(new InputStreamReader(this.getInputStream()));
        output = new PrintWriter(this.getOutputStream(), true);
        message_buffer = new ArrayList<String>();
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
    
    /**
     * Gets an array of messages that have been received from the socket in the order they were received.
     * Messages are separated by a newline character.
     * @return Messages received in order.
     */
    public synchronized String[] get_messages() {
        String[] strings = message_buffer.toArray(new String[0]);
        message_buffer.clear();
        return strings;
    }
    
    /**
     * Sends the given message through the socket.
     * @param message The message to be sent. An additional newline character will separate this message from future messages.
     */
    public void send(String message) {
        output.println(message);
    }
    
    private synchronized void add_message(String message) {
        message_buffer.add(message);
    }
}
