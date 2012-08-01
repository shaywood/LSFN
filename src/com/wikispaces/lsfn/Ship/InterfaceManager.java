package com.wikispaces.lsfn.Ship;

import java.util.Calendar;
import java.util.HashMap;

public class InterfaceManager {
    HashMap<Integer, NetworkState> INTStates;
    HashMap<Integer, Long> INTConnectionTime;
    ShipNetworking network;
    Integer newConnectionTimeout;
    
    private enum NetworkState {
        NEW,
        CONNECTED,
        DISCONNECTED_CLEAN,
        DISCONNECTED_UNCLEAN
    }
    
    public InterfaceManager(ShipNetworking network, Integer newConnectionTimeout) {
        INTStates = new HashMap<Integer, NetworkState>();
        this.newConnectionTimeout = newConnectionTimeout;
    }
    
    public void handleNewConnections() {
        for(Integer i : network.getNewINTConnections()) {
            INTStates.put(i, NetworkState.NEW);
            INTConnectionTime.put(i, Calendar.getInstance().getTimeInMillis());
        }
        timeoutSilentNewConnections();
    }
    
    private void timeoutSilentNewConnections() {
        for(Integer i : INTStates.keySet()) {
            if(INTStates.get(i) == NetworkState.NEW) {
                if(Calendar.getInstance().getTimeInMillis() - INTConnectionTime.get(i) > newConnectionTimeout) {
                    network.disconnectINT(i);
                    INTStates.remove(i);
                    INTConnectionTime.remove(i);
                }
            }
        }
    }
    
    public void handleHandshake() {
        
    }
}
