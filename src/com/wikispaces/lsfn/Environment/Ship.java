package com.wikispaces.lsfn.Environment;

import java.util.*;
import com.wikispaces.lsfn.Shared.LSFN.*;

public class Ship {
    private int ID;
    private double x, y;
    private double v_x, v_y;
    private double a_x, a_y;
    
    private static Map<Integer, Ship> ships = new HashMap<Integer, Ship>();
    private static int next_ship_ID = 0;
    
    Ship(double x, double y) {
        this.ID = next_ship_ID++;
        this.x = x;
        this.y = y;
        v_x = 0;
        v_y = 0;
        a_x = 0;
        a_y = 0;
        
        ships.put(ID, this);
    }
    
    /**
     * This interprets and applies the data received from the SHIP.
     * @param data The data received.
     */
    public void data_from_SHIP(SE.Ship_movement data) {
        if(data.getAxisAccelCount() >= 2) {
            a_x = data.getAxisAccel(0);
            a_y = data.getAxisAccel(1);
        }
    }
    
    public static void data_from_SHIPs(Integer ship_ID, SE.Ship_movement data) {
        ships.get(ship_ID).data_from_SHIP(data);
    }
    
    public ES.Ship_positions.Ship_position get_proto_position() {
        ES.Ship_positions.Ship_position position = ES.Ship_positions.Ship_position.newBuilder()
            .setShipID(ID)
            .addCoordinates(x)
            .addCoordinates(y)
            .build();
        return position;
    }
    
    public static ES.Ship_positions get_proto_positions() {
        ArrayList<ES.Ship_positions.Ship_position> ship_positions = new ArrayList<ES.Ship_positions.Ship_position>();
        Iterator<Integer> ship_iterator = ships.keySet().iterator();
        while(ship_iterator.hasNext()) {
            Ship current_ship = ships.get(ship_iterator.next());
            ship_positions.add(current_ship.get_proto_position());
        }
        ES.Ship_positions positions = ES.Ship_positions.newBuilder()
                .addAllPositions(ship_positions)
                .build();
        return positions;
    }
    
    public int get_ID() {
        return ID;
    }
    
    public void set_x(double x) {
        this.x = x;
    }
    
    public double get_x() {
        return x;
    }
    
    public void set_y(double y) {
        this.y = y;
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
    
    public static void tick_all() {
        Iterator<Integer> ship_iterator = ships.keySet().iterator();
        while(ship_iterator.hasNext()) {
            ships.get(ship_iterator.next()).tick();
        }
    }
    
    public void clamp(int width, int height) {
        if(x < 0) {
            x = 0;
        } else if(x > width - 1) {
            x = width - 1;
        }
        if(y < 0) {
            y = 0;
        } else if(y > height - 1) {
            y = height - 1;
        }
    }
    
    public static void clamp_all(int width, int height) {
        Iterator<Integer> ship_iterator = ships.keySet().iterator();
        while(ship_iterator.hasNext()) {
            ships.get(ship_iterator.next()).clamp(width, height);
        }
    }
}
