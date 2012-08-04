package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.wikispaces.lsfn.Interface.InterfaceClient;

@SuppressWarnings("serial")
public class ShipConnect extends ConnectButtonWrapperPanel {

	public ShipConnect(InterfaceClient client, JPanel wrapped) {
		super(client, wrapped, 14612, "Connect SHIP");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//client.start_SHIP_client(host.getText(), Integer.parseInt(port.getText()));
		connect.setEnabled(false);
	}
}