package com.wikispaces.lsfn.Environment;

import com.wikispaces.lsfn.Shared.LSFN.IS;
import com.wikispaces.lsfn.Shared.LSFN.SE;
import java.io.*;
import java.util.*;

public class EnvironmentServer implements Runnable {
    private EnvironmentNetworking network;
    private boolean running;
    private BufferedReader stdin;
    
    EnvironmentServer() {
        network = new EnvironmentNetworking(500);
        stdin = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public void run() {
        if(!network.openSHIPServer()) {
            System.out.println("Failed to open server.");
        }
        
        running = true;
        while(running) {
        	processStdin();
            
        	network.handleConnectionUpdates();
            processSHIPs();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO
                e.printStackTrace();
            }
        }
        
        network.closeSHIPServer();
    }
    
    private void processStdin() {
        try {
            while(stdin.ready()) {
                processStdinMessage(stdin.readLine());
            }
        } catch (IOException e) {
            System.err.println("Failed to read from stdin.");
            e.printStackTrace();
            running = false;
        }
    }
    
    private void processStdinMessage(String message) {
        if(message.equals("stop")) running = false;
    }
    
    private void processSHIPs() {    	
        HashMap<Integer, SE[]> allMessages = network.readAllFromSHIPs();
        for(Integer SHIPID : allMessages.keySet()) {
            SE[] messages = allMessages.get(SHIPID);
            if(messages == null) continue;
            
            for(int i = 0; i < messages.length; i++) {
                processSHIPMessage(SHIPID, messages[i]);
            }
        }
    }
    
    private void processSHIPMessage(Integer SHIPID, SE message) {
        if(message != null) {
            if(message.hasRcon()) {
                processStdinMessage(message.getRcon());
            }
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnvironmentServer().run();
    }
}
