/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.terminal;

import java.util.Map;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;

public class ArduinoTerminalConfigPanel extends AbstractExtendedConfigurationPanel {

	private ArduinoTerminalSettings settings;
	private ArduinoTerminalSettingsPage page;

	public ArduinoTerminalConfigPanel(IConfigurationPanelContainer container) {
		super(container);
	}

	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		settings = new ArduinoTerminalSettings();
		page = new ArduinoTerminalSettingsPage(settings, this);
		page.createControl(panel);

		createEncodingUI(panel, true);

		setControl(panel);
	}

	@Override
	public void extractData(Map<String, Object> data) {
		if (data == null) {
			return;
		}

		page.saveSettings();
		data.put(ArduinoTerminalSettings.BOARD_ATTR, settings.getBoardName());
		data.put(SerialSettings.PORT_NAME_ATTR, settings.getPortName());
		data.put(SerialSettings.BAUD_RATE_ATTR, settings.getBaudRate());
		data.put(SerialSettings.BYTE_SIZE_ATTR, settings.getByteSize());
		data.put(SerialSettings.PARITY_ATTR, settings.getParity());
		data.put(SerialSettings.STOP_BITS_ATTR, settings.getStopBits());

		if (getEncoding() != null) {
			data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
		}
	}

	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null) {
			return;
		}

		settings.setBoardName((String) data.get(ArduinoTerminalSettings.BOARD_ATTR));
		settings.setPortName((String) data.get(SerialSettings.PORT_NAME_ATTR));
		settings.setBaudRate((BaudRate) data.get(SerialSettings.BAUD_RATE_ATTR));
		settings.setByteSize((ByteSize) data.get(SerialSettings.BYTE_SIZE_ATTR));
		settings.setParity((Parity) data.get(SerialSettings.PARITY_ATTR));
		settings.setStopBits((StopBits) data.get(SerialSettings.STOP_BITS_ATTR));

		String encoding = (String) data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (encoding != null) {
			setEncoding(encoding);
		}
	}

	@Override
	protected void saveSettingsForHost(boolean add) {
	}

	@Override
	protected void fillSettingsForHost(String host) {
	}

	@Override
	protected String getHostFromSettings() {
		if (page != null) {
			page.saveSettings();
			return settings.getPortName();
		}
		return null;
	}
}
