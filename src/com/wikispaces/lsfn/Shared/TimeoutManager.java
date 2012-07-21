package com.wikispaces.lsfn.Shared;

import java.util.Calendar;

public class TimeoutManager {
    private long timeLastSent;
    private long timeLastReceived;
    private long declareAliveWait;
    private long pingTimeout;
    
    TimeoutManager(long aliveWait, long timeout) {
        timeLastSent = Calendar.getInstance().getTimeInMillis();
        timeLastReceived = Calendar.getInstance().getTimeInMillis();
        declareAliveWait = aliveWait;
        pingTimeout = timeout;
        if(declareAliveWait > pingTimeout) {
            declareAliveWait = pingTimeout / 2;
        }
    }
    
    public void sendOccured() {
        timeLastSent = Calendar.getInstance().getTimeInMillis();
    }
    
    public void receiveOccured() {
        timeLastReceived = Calendar.getInstance().getTimeInMillis();
    }
    
    public boolean shouldDeclareAlive() {
        return Calendar.getInstance().getTimeInMillis() - timeLastSent >= declareAliveWait;
    }
    
    public boolean shouldTimeout() {
        return Calendar.getInstance().getTimeInMillis() - timeLastReceived >= pingTimeout;
    }
}
