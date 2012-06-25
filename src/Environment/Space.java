import java.util.HashMap;
import java.util.Iterator;

public class Space {
    HashMap<Integer, Ship> ships;
    private int width, height;
    
    Space(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void data_from_SHIPs(HashMap<Integer, String[]> data) {
        Iterator<Integer> ship_ID_iterator = data.keySet().iterator();
        while(ship_ID_iterator.hasNext()) {
            ships.get(ship_ID_iterator.next()).data_from_SHIP(data.get(ship_ID_iterator.next()));
        }
    }
    
    public void tick() {
        Iterator<Integer> ship_ID_iterator = ships.keySet().iterator();
        while(ship_ID_iterator.hasNext()) {
            Ship current_ship = ships.get(ship_ID_iterator.next());
            current_ship.tick();
            clamp_to_universe(current_ship);
        }
    }
    
    private void clamp_to_universe(Ship ship) {
        if(ship.get_x() < 0) {
            ship.set_x(0);
        } else if(ship.get_x() > width - 1) {
            ship.set_x(width - 1);
        }
        if(ship.get_y() < 0) {
            ship.set_y(0);
        } else if(ship.get_y() > height - 1) {
            ship.set_y(height - 1);
        }
    }
}
