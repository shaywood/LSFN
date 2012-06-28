package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.Color;
import java.awt.Graphics2D;

public class ShipDot implements Drawable {
	public void draw(Graphics2D drawOn) {
		drawOn.setColor(Color.WHITE);
		drawOn.fillOval(64, 64, 8, 8);
	}
}
