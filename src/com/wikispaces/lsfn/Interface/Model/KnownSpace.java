package com.wikispaces.lsfn.Interface.Model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


public class KnownSpace {
	List<Ship> other_ships = new ArrayList<Ship>();
	Ship our_ship;
	
	public KnownSpace()	{
	}
	
	public Ship get_our_ship() {
		return our_ship;
	}
	
	public void set_our_ship(Ship our_ship) {
		this.our_ship = our_ship;
	}
	
	public List<Ship> get_other_known_ships() {
		return other_ships;
	}
	
	public void update(double time_step) {
			if(is_initialized()) {
			for (Ship s : other_ships) {
				s.update(time_step);
			}
			our_ship.update(time_step);
			// ToDo: clear out ships which have left sensor range. We may prefer to keep them on as sensor ghosts for a while, so that players can guess where they might be based on their last known trajectory.
		}
	}
	
	public void new_ship_data(int id, Point2D position, Point2D velocity) {
		if(our_ship == null) {
			throw new RuntimeException("Attempted to update the world with coordinates without first setting up our own ship.");
		}
		if (id == our_ship.get_id()) {
			our_ship.new_data(position, velocity);
		}
		else if(other_ships.contains(id)) {
			other_ships.get(id).new_data(position, velocity);
		}
		else {
			other_ships.add(new Ship(id, position, velocity));
		}
		// ToDo: we probably need to timestamp our data (or perhaps include a tick number), so that we can account for network latency here by running something like ship.update(latency_time).
	}
	
	public boolean is_initialized() {
		return our_ship != null;
	}
}
