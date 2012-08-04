package com.wikispaces.lsfn.Interface.Model;

import java.awt.geom.Point2D;

public class DummyUniverse extends KnownSpace {
	public DummyUniverse() {
		super();
		this.set_our_ship(new Ship(1, new Point2D.Double(256.0, 256.0), new Point2D.Double(30.0, 30.0)));
		this.new_ship_data(2, new Point2D.Double(512.0, 512.0), new Point2D.Double(-15.0, -30.0));
	}
}
