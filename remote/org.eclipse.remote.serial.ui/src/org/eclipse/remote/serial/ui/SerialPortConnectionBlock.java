/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.serial.core.ISerialPortService;
import org.eclipse.remote.serial.internal.ui.Activator;
import org.eclipse.remote.serial.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SerialPortConnectionBlock {

	private String name;
	private String portName;
	private int baudRateIndex;
	private int byteSizeIndex;
	private int parityIndex;
	private int stopBitsIndex;

	private String[] portNames;

	private Text nameText;
	private Combo portCombo;
	private Combo baudRateCombo;
	private Combo byteSizeCombo;
	private Combo parityCombo;
	private Combo stopBitsCombo;

	private boolean isComplete;
	private List<SerialBlockUpdateListener> listeners = new ArrayList<>();

	/**
	 * Creates the UI elements for the SerialPortConnectionBlock
	 *
	 * @param comp - parent composite
	 * @param wc - an IRemoteConnectionWorkingCopy to populate the default values from. Can be null.
	 */
	public void createBlock(Composite comp, IRemoteConnectionWorkingCopy wc) {

		String name = "";
		String connectionPortName = "";
		int baudRateStringIndex = BaudRate.getStringIndex(BaudRate.getDefault());
		int byteSizeStringIndex = ByteSize.getStringIndex(ByteSize.getDefault());
		int parityStringIndex = Parity.getStringIndex(Parity.getDefault());
		int stopBitsStringIndex = StopBits.getStringIndex(StopBits.getDefault());

		if (wc != null) {
			name = wc.getName();
			connectionPortName = wc.getAttribute(ISerialPortService.PORT_NAME_ATTR);
			baudRateStringIndex = Integer.parseInt(wc.getAttribute(ISerialPortService.BAUD_RATE_ATTR));
			byteSizeStringIndex = Integer.parseInt(wc.getAttribute(ISerialPortService.BYTE_SIZE_ATTR));
			parityStringIndex = Integer.parseInt(wc.getAttribute(ISerialPortService.PARITY_ATTR));
			stopBitsStringIndex = Integer.parseInt(wc.getAttribute(ISerialPortService.STOP_BITS_ATTR));
		}

		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText(Messages.NewSerialPortConnectionWizardPage_NameLabel);

		nameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(name); //$NON-NLS-1$
		nameText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.NewSerialPortConnectionWizardPage_PortLabel);

		portCombo = new Combo(comp, SWT.READ_ONLY);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			portNames = SerialPort.list();
		} catch (IOException e) {
			Activator.log(e);
		}
		int index = 0;
		int portNameIndex = 0;
		for (String portName : portNames) {
			portCombo.add(portName);
			if (portName.equals(connectionPortName))
				portNameIndex = index;
			index++;
		}
		portCombo.select(portNameIndex);
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label baudRateLabel = new Label(comp, SWT.NONE);
		baudRateLabel.setText(Messages.NewSerialPortConnectionWizardPage_BaudRateLabel);

		baudRateCombo = new Combo(comp, SWT.READ_ONLY);
		baudRateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String baudRateStr : BaudRate.getStrings()) {
			baudRateCombo.add(baudRateStr);
		}

		baudRateCombo.select(baudRateStringIndex);
		baudRateCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label byteSizeLabel = new Label(comp, SWT.NONE);
		byteSizeLabel.setText(Messages.NewSerialPortConnectionWizardPage_ByteSizeLabel);

		byteSizeCombo = new Combo(comp, SWT.READ_ONLY);
		byteSizeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String byteSizeStr : ByteSize.getStrings()) {
			byteSizeCombo.add(byteSizeStr);
		}
		byteSizeCombo.select(byteSizeStringIndex);
		byteSizeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label parityLabel = new Label(comp, SWT.NONE);
		parityLabel.setText(Messages.NewSerialPortConnectionWizardPage_ParityLabel);

		parityCombo = new Combo(comp, SWT.READ_ONLY);
		parityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String parityStr : Parity.getStrings()) {
			parityCombo.add(parityStr);
		}
		parityCombo.select(parityStringIndex);
		parityCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Label stopBitsLabel = new Label(comp, SWT.NONE);
		stopBitsLabel.setText(Messages.NewSerialPortConnectionWizardPage_StopBitsLabel);

		stopBitsCombo = new Combo(comp, SWT.READ_ONLY);
		stopBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String stopBitsStr : StopBits.getStrings()) {
			stopBitsCombo.add(stopBitsStr);
		}
		stopBitsCombo.select(stopBitsStringIndex);
		stopBitsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		updateStatus();
	}

	private void updateStatus() {
		name = nameText.getText();

		int portIndex = portCombo.getSelectionIndex();
		portName = portIndex < 0 ? null : portNames[portIndex];

		baudRateIndex = baudRateCombo.getSelectionIndex();
		byteSizeIndex = byteSizeCombo.getSelectionIndex();
		parityIndex = parityCombo.getSelectionIndex();
		stopBitsIndex = stopBitsCombo.getSelectionIndex();

		isComplete = (!name.isEmpty() && portName != null);

		for (SerialBlockUpdateListener listener : listeners) {
			listener.update();
		}
	}

	public String getConnectionName() {
		return name;
	}

	public String getPortName() {
		return portName;
	}

	public int getBaudRateIndex() {
		return baudRateIndex;
	}

	public int getByteSizeIndex() {
		return byteSizeIndex;
	}

	public int getParityIndex() {
		return parityIndex;
	}

	public int getStopBitsIndex() {
		return stopBitsIndex;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void addUpdateListener(SerialBlockUpdateListener listener) {
		if (listener != null && !listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeUpdateListener(SerialBlockUpdateListener listener) {
		listeners.remove(listener);
	}

	public abstract class SerialBlockUpdateListener {
		public abstract void update();
	}
}
