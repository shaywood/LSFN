import java.io.*;

public class InterfaceClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String host_str = "localhost";
        int port = 14612;
        Listener listener = null;;
        try {
            listener = new Listener(host_str, port);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        // Check if socket creation succeeded
        if(listener != null) {
            Thread listen_thread = new Thread(listener);
            listen_thread.start();
            boolean running = true;
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
        
            while(running) {
                userInput = "";
                // First we try to read some input from stdin if there is any.
                try {
                    if(stdIn.ready()) {
                        // If there is we send it to the server.
                        userInput = stdIn.readLine();
                        listener.send(userInput);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read from stdin.");
                    e.printStackTrace();
                    running = false;
                }
                
                // Then we get any messages the server has sent to us, if any.
                String[] messages = listener.get_messages();
                for(int i = 0; i < messages.length; i++) {
                    System.out.println("Received message \"" + messages[i] + "\"");
                    if(messages[i].equals("Server shutting down.")) running = false;
                }
                
                // Lastly, if we haven't told the program to stop, we sleep for 1/50 seconds (20ms)
                if(running && userInput.equals("Bye.")) {
                    running = false;
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO
                        e.printStackTrace();
                    }
                }
            }
            
            // When we shut down, we close the listener and join the thread.
            System.out.println("Shutting down.");
            try {
                listen_thread.interrupt();
                listen_thread.join();
            } catch (InterruptedException e) {
                // TODO
                e.printStackTrace();
            }
            
            try {
                listener.close();
            } catch (IOException e1) {
                // TODO
                e1.printStackTrace();
            }
        }
    }

}