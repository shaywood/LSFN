package com.wikispaces.lsfn.Interface.Model;

import java.awt.geom.Point2D;

public class DummyUniverse extends KnownSpace {
	public DummyUniverse() {
		super(new Ship(1, new Point2D.Double(64.0, 64.0), new Point2D.Double(1.0, 1.0)));
		Ship another_ship = new Ship(2, new Point2D.Double(128.0, 128.0), new Point2D.Double(-0.5, -1.0));
		super.ships.add(another_ship);
	}
}
