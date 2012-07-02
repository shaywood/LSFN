package com.wikispaces.lsfn.Environment;

public class Ship {
    private double x, y;
    private double v_x, v_y;
    private double a_x, a_y;
    
    Ship(double x, double y) {
        this.x = x;
        this.y = y;
        v_x = 0;
        v_y = 0;
        a_x = 0;
        a_y = 0;
    }
    
    /**
     * This interprets and applies the data received from the SHIP.
     * @param data The data received.
     */
    public void data_from_SHIP(String[] data) {
        
    }
    
    public void set_x(double x) {
        this.x = x;
    }
    
    public void set_y(double y) {
        this.y = y;
    }
    
    public double get_x() {
        return x;
    }
    
    public double get_y() {
        return y;
    }
    
    public void tick() {
        v_x += a_x;
        v_y += a_y;
        x += v_x;
        y += v_y;
    }
}
