import java.io.*;
import java.net.*;

public class InterfaceClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Socket echoSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String hostStr = "localhost";

        try {
            echoSocket = new Socket(hostStr, 14612);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostStr);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostStr);
            System.exit(1);
        }

        System.out.println("Connected to host.");

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput = "";
    
        while(true) {
            try {
               userInput = stdIn.readLine(); 
            } catch (IOException e) {
                System.err.println("Failed to read from socket.");
                break;
            }
            
            out.println(userInput);
            
            try {
                System.out.println("echo: " + in.readLine());
            } catch (IOException e) {
                System.err.println("Failed to read from socket.");
                break;
            }
            
            if(userInput.equals("Bye.")) {
                break;
            }
        }
        
        System.out.println("Shutting down.");
        
        try {
            out.close();
            in.close();
            stdIn.close();
            echoSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket / buffers.");
        }
    }

}
