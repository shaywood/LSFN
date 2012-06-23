import java.io.*;

public class InterfaceClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Listener listener = new Listener();
        String host_str = "localhost";
        int port = 14612;
        listener.set_host_and_port(host_str, port);
        Thread listen_thread = new Thread(listener);
        listen_thread.start();

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
    
        while(true) {
            userInput = "";
            try {
                if(stdIn.ready()) {
                    userInput = stdIn.readLine();
                    listener.send(userInput);
                    
                }
            } catch (IOException e) {
                System.err.println("Failed to read from stdin.");
                e.printStackTrace();
                break;
            }
            
            String[] messages = listener.get_messages();
            
            for(int i = 0; i < messages.length; i++) {
                System.out.println("Received message \"" + messages[i] + "\"");
            }
            
            if(userInput.equals("Bye.")) {
                break;
            }
        }
        
        System.out.println("Shutting down.");
        
        listener.close();
        try {
            listen_thread.join();
        } catch (InterruptedException e) {
            // TODO
            e.printStackTrace();
        }
    }

}
