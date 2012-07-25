package com.wikispaces.lsfn.Interface.Display2D;

import com.wikispaces.lsfn.Interface.Model.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class SpacePanel extends JPanel {
	KnownSpace world;
	ShipDot draw_ships = new ShipDot();
	Point2D centre_point;

	public SpacePanel(KnownSpace world, Point2D centre_point) {
		setBackground(Color.BLACK);
		this.world = world;
		this.centre_point = centre_point;
	}
	
	//Overridden method, called every time the component is repainted.
	public void paintComponent (Graphics draw_here) {
		Graphics2D draw_on = (Graphics2D) draw_here;
		super.paintComponent(draw_on);
		
		if(world.is_initialized()) {
			centre_view_on_point(draw_on, world.get_our_ship().get_position());
	
			draw_ships.draw(draw_on, world.get_our_ship().get_position(), Color.GREEN);
			for (Ship s : world.get_other_known_ships()) {
				draw_ships.draw(draw_on, s.get_position(), Color.RED);
			}
		}
	}
	
	private void centre_view_on_point(Graphics2D draw_on, Point2D centre_on) {
		draw_on.translate(centre_point.getX() - centre_on.getX(), centre_point.getY() - centre_on.getY());
	}
}
