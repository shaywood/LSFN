package com.wikispaces.lsfn.Interface.Display2D;

import com.wikispaces.lsfn.Interface.Model.*;

import java.awt.geom.Point2D;

import javax.swing.JFrame;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class MapDisplay extends JFrame {
	int width = 1024;
	int height = 1024;
	
	public MapDisplay(KnownSpace world) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(width, height);
		getRootPane().setDoubleBuffered(true);
		getContentPane().add(new SpacePanel(world, new Point2D.Double(width/2, height/2)));
		setVisible(true);
	}
	
	// A registered component signals that the Frame needs repainting.
	public void step () {
		repaint();
	}
}
