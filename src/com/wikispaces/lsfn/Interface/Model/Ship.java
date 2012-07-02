package com.wikispaces.lsfn.Interface.Model;

import java.awt.geom.Point2D;

public class Ship {
	int id;
	Point2D position;
	Point2D velocity;
	
	public Ship(int id, Point2D position, Point2D velocity) {
		this.id = id;
		this.position = position;
		this.velocity = velocity;
	}
	
	public int get_id() {
		return id;
	}
	
	public Point2D get_position() {
		return position;
	}
	
	
	// time_step should be 1/(frequency of updates).
	public void update(double time_step) {
		position = new Point2D.Double(
			position.getX() + (velocity.getX() * time_step),
			position.getY() + (velocity.getY() * time_step)
		);
	}
	
	public void new_data(Point2D position, Point2D velocity) {
		this.position = position;
		this.velocity = velocity;
	}
}
