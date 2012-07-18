package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;


import com.wikispaces.lsfn.Interface.InterfaceClient;
import com.wikispaces.lsfn.Interface.Model.KnownSpace;
import com.wikispaces.lsfn.Shared.Subscription.Subscribeable;

@SuppressWarnings("serial") // not intending to serialize UI elements
public class MapDisplay extends JFrame {
	int width = 1024;
	int height = 1024;
	
	public MapDisplay(InterfaceClient client, BlockingQueue<Subscribeable> player_input_queue, KnownSpace world) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(width, height);
		getRootPane().setDoubleBuffered(true);
		SpacePanel panel = new SpacePanel(world, new Point2D.Double(width/2, height/2));
		new ShipMovement(player_input_queue, panel, Arrays.asList(Controls.values()));
		
		// It's pretty bad to be passing the whole interface client passing through here, but since the ConnectButtonWrapperPanel is a temporary quick-fix for testing purposes that we will tear out at some point, I've left it this way. If we find ourselves needing to use the InterfaceClient for anything else in here, then we should pull some bits out into new classes.
		getContentPane().add(new ConnectButtonWrapperPanel(client, panel));
		
		setVisible(true);
	}
	
	// A registered component signals that the Frame needs repainting.
	public void step () {
		repaint();
	}
}
