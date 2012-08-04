package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

public class ShipDot {
	public void draw(Graphics2D drawOn, Point2D position, Color colour) {
		drawOn.setColor(colour);
		drawOn.fillOval((int)position.getX(), (int)position.getY(), 8, 8);
	}
}
