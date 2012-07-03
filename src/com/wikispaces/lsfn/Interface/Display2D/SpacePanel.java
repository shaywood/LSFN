package com.wikispaces.lsfn.Interface.Display2D;

import com.wikispaces.lsfn.Interface.Model.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class SpacePanel extends JPanel {
	KnownSpace world;
	ShipDot draw_ships = new ShipDot();

	public SpacePanel(KnownSpace world) {
		setBackground(Color.BLACK);
		this.world = world;
	}

	//Overridden method, called every time the component is repainted.
	public void paintComponent (Graphics drawHere) {
		Graphics2D drawOn = (Graphics2D) drawHere;
		super.paintComponent(drawOn);
		
		// ToDo: make the view centre on our ship
		//Vector2D view_centre = world.get_our_ship().get_position();

		for (Ship s : world.get_known_ships()) {
			draw_ships.draw(drawOn, s.get_position());
		}
	}
}
