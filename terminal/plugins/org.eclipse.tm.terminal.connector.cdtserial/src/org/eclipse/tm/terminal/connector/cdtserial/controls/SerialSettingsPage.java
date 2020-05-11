/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.cdtserial.controls;

import java.io.IOException;

import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StandardBaudRates;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.terminal.connector.cdtserial.activator.Activator;
import org.eclipse.tm.terminal.connector.cdtserial.connector.SerialConnector;
import org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings;
import org.eclipse.tm.terminal.connector.cdtserial.nls.Messages;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;

public class SerialSettingsPage extends AbstractSettingsPage {

	private final SerialSettings settings;
	private final IConfigurationPanel panel;
	private final IDialogSettings dialogSettings;

	private Combo portCombo;
	private Combo baudRateCombo;
	private Combo byteSizeCombo;
	private Combo parityCombo;
	private Combo stopBitsCombo;

	private String portName;
	private int baudRate;
	private ByteSize byteSize;
	private Parity parity;
	private StopBits stopBits;

	public SerialSettingsPage(SerialSettings settings, IConfigurationPanel panel) {
		this.settings = settings;
		this.panel = panel;
		setHasControlDecoration(true);

		dialogSettings = DialogSettings.getOrCreateSection(Activator.getDefault().getDialogSettings(),
				this.getClass().getSimpleName());
		portName = dialogSettings.get(SerialSettings.PORT_NAME_ATTR);

		String baudRateStr = dialogSettings.get(SerialSettings.BAUD_RATE_ATTR);
		if (baudRateStr != null && !baudRateStr.isEmpty()) {
			try {
				baudRate = Integer.parseInt(baudRateStr);
			} catch (NumberFormatException e) {
			}
		}
		if (baudRate <= 0) {
			baudRate = StandardBaudRates.getDefault();
		}

		String byteSizeStr = dialogSettings.get(SerialSettings.BYTE_SIZE_ATTR);
		if (byteSizeStr == null || byteSizeStr.isEmpty()) {
			byteSize = ByteSize.getDefault();
		} else {
			String[] sizes = ByteSize.getStrings();
			for (int i = 0; i < sizes.length; ++i) {
				if (byteSizeStr.equals(sizes[i])) {
					byteSize = ByteSize.fromStringIndex(i);
					break;
				}
			}
		}

		String parityStr = dialogSettings.get(SerialSettings.PARITY_ATTR);
		if (parityStr == null || parityStr.isEmpty()) {
			parity = Parity.getDefault();
		} else {
			String[] parities = Parity.getStrings();
			for (int i = 0; i < parities.length; ++i) {
				if (parityStr.equals(parities[i])) {
					parity = Parity.fromStringIndex(i);
					break;
				}
			}
		}

		String stopBitsStr = dialogSettings.get(SerialSettings.STOP_BITS_ATTR);
		if (stopBitsStr == null || stopBitsStr.isEmpty()) {
			stopBits = StopBits.getDefault();
		} else {
			String[] bits = StopBits.getStrings();
			for (int i = 0; i < bits.length; ++i) {
				if (stopBitsStr.equals(bits[i])) {
					stopBits = StopBits.fromStringIndex(i);
					break;
				}
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(gridLayout);
		comp.setLayoutData(gridData);

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.SerialTerminalSettingsPage_SerialPort);

		portCombo = new Combo(comp, SWT.NONE);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] portNames = new String[0];
		try {
			portNames = SerialPort.list();
		} catch (IOException e) {
			Activator.log(e);
		}
		for (String portName : portNames) {
			if (!SerialConnector.isOpen(portName)) {
				portCombo.add(portName);
			}
		}
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
			}
		});

		Label baudRateLabel = new Label(comp, SWT.NONE);
		baudRateLabel.setText(Messages.SerialTerminalSettingsPage_BaudRate);

		baudRateCombo = new Combo(comp, SWT.DROP_DOWN);
		baudRateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		baudRateCombo.setItems(StandardBaudRates.asStringArray());

		Label byteSizeLabel = new Label(comp, SWT.NONE);
		byteSizeLabel.setText(Messages.SerialTerminalSettingsPage_DataSize);

		byteSizeCombo = new Combo(comp, SWT.READ_ONLY);
		byteSizeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String byteSizeStr : ByteSize.getStrings()) {
			byteSizeCombo.add(byteSizeStr);
		}

		Label parityLabel = new Label(comp, SWT.NONE);
		parityLabel.setText(Messages.SerialTerminalSettingsPage_Parity);

		parityCombo = new Combo(comp, SWT.READ_ONLY);
		parityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String parityStr : Parity.getStrings()) {
			parityCombo.add(parityStr);
		}

		Label stopBitsLabel = new Label(comp, SWT.NONE);
		stopBitsLabel.setText(Messages.SerialTerminalSettingsPage_StopBits);

		stopBitsCombo = new Combo(comp, SWT.READ_ONLY);
		stopBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String stopBitsStr : StopBits.getStrings()) {
			stopBitsCombo.add(stopBitsStr);
		}

		loadSettings();
	}

	void validate() {
		IConfigurationPanelContainer container = panel.getContainer();
		container.validate();
	}

	@Override
	public void loadSettings() {
		String portName = settings.getPortName();
		if (portName == null || portName.isEmpty()) {
			portName = this.portName;
		}
		if (portName != null && !portName.isEmpty() && !SerialConnector.isOpen(portName)) {
			int i = 0;
			for (String name : portCombo.getItems()) {
				if (portName.equals(name)) {
					portCombo.select(i);
					break;
				}
				i++;
			}
		} else if (portCombo.getItemCount() > 0) {
			portCombo.select(0);
		}

		int baudRate = settings.getBaudRateValue();
		if (baudRate <= 0) {
			baudRate = this.baudRate;
		}
		baudRateCombo.setText(Integer.toString(baudRate));

		ByteSize byteSize = settings.getByteSize();
		if (byteSize == null) {
			byteSize = this.byteSize;
		}
		byteSizeCombo.select(ByteSize.getStringIndex(byteSize));

		Parity parity = settings.getParity();
		if (parity == null) {
			parity = this.parity;
		}
		parityCombo.select(Parity.getStringIndex(parity));

		StopBits stopBits = settings.getStopBits();
		if (stopBits == null) {
			stopBits = this.stopBits;
		}
		stopBitsCombo.select(StopBits.getStringIndex(stopBits));
	}

	@Override
	public void saveSettings() {
		settings.setPortName(portCombo.getText());
		int baudRateValue = 0;
		try {
			baudRateValue = Integer.parseInt(baudRateCombo.getText());
		} catch (NumberFormatException e) {
		}
		if (baudRateValue <= 0) {
			baudRateValue = StandardBaudRates.getDefault();
		}
		settings.setBaudRateValue(baudRateValue);
		settings.setByteSize(ByteSize.fromStringIndex(byteSizeCombo.getSelectionIndex()));
		settings.setParity(Parity.fromStringIndex(parityCombo.getSelectionIndex()));
		settings.setStopBits(StopBits.fromStringIndex(stopBitsCombo.getSelectionIndex()));

		dialogSettings.put(SerialSettings.PORT_NAME_ATTR, portCombo.getText());
		dialogSettings.put(SerialSettings.BAUD_RATE_ATTR, Integer.toString(baudRateValue));
		dialogSettings.put(SerialSettings.BYTE_SIZE_ATTR, ByteSize.getStrings()[byteSizeCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.PARITY_ATTR, Parity.getStrings()[parityCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.STOP_BITS_ATTR, StopBits.getStrings()[stopBitsCombo.getSelectionIndex()]);
	}

	@Override
	public boolean validateSettings() {
		if (portCombo.getSelectionIndex() < 0 && portCombo.getText().isEmpty()) {
			return false;
		}
		return true;
	}

}
