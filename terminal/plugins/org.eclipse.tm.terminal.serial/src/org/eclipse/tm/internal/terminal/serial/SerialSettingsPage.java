/*******************************************************************************
 * Copyright (c) 2003, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - extracted from TerminalSettingsDlg
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206917] Add validation for Terminal Settings
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.serial;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;

public class SerialSettingsPage implements ISettingsPage {
	private Combo fSerialPortCombo;
	private Combo fBaudRateCombo;
	private Combo fDataBitsCombo;
	private Combo fStopBitsCombo;
	private Combo fParityCombo;
	private Combo fFlowControlCombo;
	private Text fTimeout;
	private final SerialSettings fTerminalSettings;

	public SerialSettingsPage(SerialSettings settings) {
		fTerminalSettings=settings;
	}
	public void saveSettings() {
		fTerminalSettings.setSerialPort(getComboValue(fSerialPortCombo));
		fTerminalSettings.setBaudRate(getComboValue(fBaudRateCombo));
		fTerminalSettings.setDataBits(getComboValue(fDataBitsCombo));
		fTerminalSettings.setStopBits(getComboValue(fStopBitsCombo));
		fTerminalSettings.setParity(getComboValue(fParityCombo));
		fTerminalSettings.setFlowControl(getComboValue(fFlowControlCombo));
		fTerminalSettings.setTimeout(fTimeout.getText());
	}
	public void loadSettings() {
		// Load controls
		SerialProperties properties = fTerminalSettings.getProperties();
		List list;

		list = properties.getSerialPortTable();
		loadCombo(fSerialPortCombo, list);

		list = properties.getBaudRateTable();
		loadCombo(fBaudRateCombo, list);

		list = properties.getDataBitsTable();
		loadCombo(fDataBitsCombo, list);

		list = properties.getStopBitsTable();
		loadCombo(fStopBitsCombo, list);

		list = properties.getParityTable();
		loadCombo(fParityCombo, list);

		list = properties.getFlowControlTable();
		loadCombo(fFlowControlCombo, list);

		setCombo(fSerialPortCombo,fTerminalSettings.getSerialPort());
		setCombo(fBaudRateCombo,fTerminalSettings.getBaudRateString());
		setCombo(fDataBitsCombo,fTerminalSettings.getDataBitsString());
		setCombo(fStopBitsCombo,fTerminalSettings.getStopBitsString());
		setCombo(fParityCombo,fTerminalSettings.getParityString());
		setCombo(fFlowControlCombo,fTerminalSettings.getFlowControlString());
		fTimeout.setText(fTerminalSettings.getTimeoutString());
	}
	private void setCombo(Combo combo,String value) {
		if(value==null)
			return;
		int nIndex = combo.indexOf(value);
		if (nIndex == -1) {
			if((combo.getStyle() & SWT.READ_ONLY)==0) {
				combo.add(value);
				nIndex = combo.indexOf(value);
			} else {
				return;
			}
		}

		combo.select(nIndex);

	}
	private String getComboValue(Combo combo) {
		int nIndex = combo.getSelectionIndex();
		if (nIndex == -1) {
			if((combo.getStyle() & SWT.READ_ONLY)!=0)
				return ""; //$NON-NLS-1$
			return combo.getText();
		}

		return combo.getItem(nIndex);

	}
	public boolean validateSettings() {
		try {
			int p = Integer.parseInt(fTimeout.getText().trim());
			if (p < 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);

		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fSerialPortCombo=addLabeledCombo(composite, SerialMessages.PORT + ":",false); //$NON-NLS-1$
		fBaudRateCombo=addLabeledCombo(composite, SerialMessages.BAUDRATE + ":"); //$NON-NLS-1$
		fDataBitsCombo=addLabeledCombo(composite, SerialMessages.DATABITS + ":"); //$NON-NLS-1$
		fStopBitsCombo=addLabeledCombo(composite, SerialMessages.STOPBITS + ":"); //$NON-NLS-1$
		fParityCombo=addLabeledCombo(composite, SerialMessages.PARITY + ":"); //$NON-NLS-1$
		fFlowControlCombo=addLabeledCombo(composite, SerialMessages.FLOWCONTROL + ":"); //$NON-NLS-1$

		new Label(composite, SWT.RIGHT).setText(SerialMessages.TIMEOUT + ":"); //$NON-NLS-1$
		fTimeout = new Text(composite, SWT.BORDER);
		fTimeout.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		loadSettings();
	}

	private Combo addLabeledCombo(Composite composite, String label) {
		return addLabeledCombo(composite, label, true);
	}
	private Combo addLabeledCombo(Composite composite, String label,boolean readonly) {
		new Label(composite, SWT.RIGHT).setText(label);
		int flags=SWT.DROP_DOWN;
		if(readonly)
			flags|=SWT.READ_ONLY;
		Combo combo = new Combo(composite, flags);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return combo;
	}

	private void loadCombo(Combo ctlCombo, List table) {
		for (Iterator iter = table.iterator(); iter.hasNext();) {
			String label = (String) iter.next();
			ctlCombo.add(label);
		}
	}
}
