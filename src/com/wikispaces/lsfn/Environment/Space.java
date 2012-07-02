package com.wikispaces.lsfn.Environment;

import java.util.HashMap;
import java.util.Iterator;

public class Space {
    private int width, height;
    
    Space(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void tick() {
        Ship.tick_all();
        Ship.clamp_all(width, height);
    }
}
