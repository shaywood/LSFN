package com.wikispaces.lsfn.Interface.Display2D;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.wikispaces.lsfn.Interface.InterfaceClient;

@SuppressWarnings("serial")
public class EnvConnect extends ConnectButtonWrapperPanel {

	public EnvConnect(InterfaceClient client, JPanel wrapped) {
		super(client, wrapped, 14613, "Connect ENV");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		client.connect_SHIP_to_ENV(host.getText(), Integer.parseInt(port.getText()));
		connect.setEnabled(false);
	}
}