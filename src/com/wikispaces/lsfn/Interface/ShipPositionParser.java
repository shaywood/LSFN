package com.wikispaces.lsfn.Interface;

import java.awt.geom.Point2D;
import java.util.List;

import com.wikispaces.lsfn.Interface.Model.KnownSpace;
import com.wikispaces.lsfn.Shared.LSFN.SI.Ship_positions;
import com.wikispaces.lsfn.Shared.LSFN.SI.Ship_positions.Ship_position;

public class ShipPositionParser {
	private final KnownSpace world;

	public ShipPositionParser(KnownSpace world) {
		this.world = world;
	}
	
	public void update_model_with_data(Ship_positions positions) {
		for (Ship_position position : positions.getPositionsList()) {
			update_model_with_data(position);
		}
	}

	private void update_model_with_data(Ship_position position) {
		 List<Double> coordinates = position.getCoordinatesList();
		 
		world.new_ship_data(position.getShipID(), new Point2D.Double(coordinates.get(0), coordinates.get(1)), new Point2D.Double(0, 0));
	}
}
