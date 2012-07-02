package com.wikispaces.lsfn.Interface.Model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


public class KnownSpace {
	List<Ship> ships = new ArrayList<Ship>();
	Ship our_ship;
	
	public KnownSpace(Ship our_ship)	{
		this.our_ship = our_ship;
		ships.add(our_ship);
	}
	
	public Ship get_our_ship() {
		return our_ship;
	}
	
	public List<Ship> get_known_ships() {
		return ships;
	}
	
	public void update(double time_step) {
		for (Ship s : ships) {
			s.update(time_step);
		}
		// ToDo: clear out ships which have left sensor range. We may prefer to keep them on as sensor ghosts for a while, so that players can guess where they might be based on their last known tragectory.
	}
	
	public void new_ship_data(int id, Point2D position, Point2D velocity) {
		if(ships.contains(id)) {
			ships.get(id).new_data(position, velocity);
		}
		else {
			ships.add(new Ship(id, position, velocity));
		}
		// ToDo: we probably need to timestamp our data (or perhaps include a tick number), so that we can account for network latency here by running something like ship.update(latency_time).
	}
}
