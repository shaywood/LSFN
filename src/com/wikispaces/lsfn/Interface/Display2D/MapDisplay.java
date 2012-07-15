package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.geom.Point2D;
import java.util.Arrays;

import javax.swing.JFrame;

import com.wikispaces.lsfn.Interface.InterfaceClient;
import com.wikispaces.lsfn.Interface.Model.KnownSpace;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class MapDisplay extends JFrame {
	int width = 1024;
	int height = 1024;
	
	public MapDisplay(InterfaceClient interface_client, KnownSpace world) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(width, height);
		getRootPane().setDoubleBuffered(true);
		SpacePanel panel = new SpacePanel(world, new Point2D.Double(width/2, height/2));
		new ShipMovement(panel, Arrays.asList(Controls.values()));
		
		getContentPane().add(new ConnectButtonWrapperPanel(interface_client, panel));
		
		setVisible(true);
	}
	
	// A registered component signals that the Frame needs repainting.
	public void step () {
		repaint();
	}
}
