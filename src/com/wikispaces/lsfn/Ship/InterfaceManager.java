package com.wikispaces.lsfn.Ship;

import java.util.Calendar;
import java.util.HashMap;

import com.wikispaces.lsfn.Shared.LSFN;
import com.wikispaces.lsfn.Shared.LSFN.SI;

public class InterfaceManager {
    HashMap<Integer, NetworkState> INTStates;
    HashMap<Integer, Long> INTConnectionTime;
    ShipNetworking network;
    Integer newConnectionTimeout;
    
    private enum NetworkState {
        NEW,
        CONNECTED
    }
    
    public InterfaceManager(ShipNetworking network, Integer newConnectionTimeout) {
        this.network = network;
        this.INTStates = new HashMap<Integer, NetworkState>();
        this.INTConnectionTime = new HashMap<Integer, Long>();
        this.newConnectionTimeout = newConnectionTimeout;
    }
    
    public void handleNewConnections() {
        Integer[] INTIDs = network.getNewINTConnections();
        for(int i = 0; i < INTIDs.length; i++) {
            System.out.println("Adding new connection");
            INTStates.put(i, NetworkState.NEW);
            INTConnectionTime.put(i, Calendar.getInstance().getTimeInMillis());
        }
        timeoutSilentNewConnections();
    }
    
    private void timeoutSilentNewConnections() {
        for(Integer i : INTStates.keySet()) {
            if(INTStates.get(i) == NetworkState.NEW) {
                if(Calendar.getInstance().getTimeInMillis() - INTConnectionTime.get(i) > newConnectionTimeout) {
                    System.out.println("Timed out connection");
                    network.disconnectINT(i);
                    INTStates.remove(i);
                    INTConnectionTime.remove(i);
                }
            }
        }
    }
    
    public void handleHandshake(Integer INTID, LSFN.IS.Handshake handshake) {
        if(handshake == LSFN.IS.Handshake.HELLO) {
            SI handshakeOut = SI.newBuilder()
                    .setHandshake(SI.Handshake.newBuilder()
                            .setType(SI.Handshake.Type.HELLO)
                            .setIntID(INTID)
                            .build())
                    .build();
            network.sendToINT(INTID, handshakeOut);
            INTConnectionTime.remove(INTID);
            INTStates.put(INTID, NetworkState.CONNECTED);
            System.out.println("New INT connected: ID=" + INTID);
        }
    }
    
    public void handleDisconnect(Integer INTID) {
        INTStates.remove(INTID);
        INTConnectionTime.remove(INTID);
        System.out.println("INT disconnected: ID=" + INTID);
    }
    
    public boolean verify(Integer INTID) {
        NetworkState state = INTStates.get(INTID);
        return state != null && state == NetworkState.CONNECTED;
    }
}
