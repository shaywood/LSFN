import java.io.*;
import java.util.*;
import java.net.*;

public class Listener implements Runnable {
    private String host_str;
    private int port;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private boolean running;
    private ArrayList<String> message_buffer;
    
    Listener() {
        socket = null;
        input = null;
        output = null;
        host_str = "";
        port = 0;
        set_running(false);
        message_buffer = new ArrayList<String>();
    }

    public void set_host_and_port(String host, int port) {
        host_str = host;
        this.port = port;
    }
    
    public void run() {
        try {
            socket = new Socket(host_str, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to host.");
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        set_running(true);
        while(is_running()) {
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
                // TODO
                e.printStackTrace();
            }
        }
        
        try {
            socket.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        input = null;
        output = null;
    }
    
    public synchronized String[] get_messages() {
        String[] strings = message_buffer.toArray(new String[0]);
        message_buffer.clear();
        return strings;
    }
    
    public synchronized void close() {
        running = false;
    }
    
    public void send(String message) {
        output.println(message);
    }
    
    private synchronized void add_message(String message) {
        message_buffer.add(message);
    }
    
    private synchronized boolean is_running() {
        return running;
    }
    
    private synchronized void set_running(boolean state) {
        running = state;
    }
}
