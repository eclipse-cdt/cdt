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
package org.eclipse.tm.internal.terminal.telnet;

import java.util.Collections;
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

public class TelnetSettingsPage implements ISettingsPage {
	private Text fHostText;
	private Combo fNetworkPortCombo;
	private Text fTimeout;
	private final TelnetSettings fTerminalSettings;

	public TelnetSettingsPage(TelnetSettings settings) {
		fTerminalSettings=settings;
	}
	public void saveSettings() {
		fTerminalSettings.setHost(fHostText.getText());
		fTerminalSettings.setTimeout(fTimeout.getText());
		fTerminalSettings.setNetworkPort(getNetworkPort());
	}

	public void loadSettings() {
		if(fTerminalSettings!=null) {
			setHost(fTerminalSettings.getHost());
			setTimeout(fTerminalSettings.getTimeoutString());
			setNetworkPort(fTerminalSettings.getNetworkPortString());
		}
	}
	private void setHost(String strHost) {
		if(strHost==null)
			strHost=""; //$NON-NLS-1$
		fHostText.setText(strHost);

	}
	private void setTimeout(String timeout) {
		if(timeout==null || timeout.length()==0)
			timeout="5"; //$NON-NLS-1$
		fTimeout.setText(timeout);

	}
	private void setNetworkPort(String strNetworkPort) {
		if (strNetworkPort!=null) {
			String strPortName = getNetworkPortMap().findPortName(strNetworkPort);
			if(strPortName==null) {
				strPortName=strNetworkPort; //fallback to verbatim port if not found
			}
			int nIndex = fNetworkPortCombo.indexOf(strPortName);

			if (nIndex == -1) {
				fNetworkPortCombo.setText(strNetworkPort);
			} else {
				fNetworkPortCombo.select(nIndex);
			}
		}
	}
	private String getNetworkPort() {
		String portText = fNetworkPortCombo.getText();
		String mappedPort = getNetworkPortMap().findPort(portText);
		return mappedPort!=null ? mappedPort : portText;
	}
	private NetworkPortMap getNetworkPortMap() {
		return fTerminalSettings.getProperties().getNetworkPortMap();
	}

	public boolean validateSettings() {
		if (fHostText.getText().trim().length() == 0) {
			return false;
		}
		try {
			int p = Integer.parseInt(getNetworkPort().trim());
			if (p <= 0 || p > 65535) {
				return false;
			}
			p = Integer.parseInt(fTimeout.getText().trim());
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
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);

		// Add label
		Label ctlLabel = new Label(composite, SWT.RIGHT);
		ctlLabel.setText(TelnetMessages.HOST + ":"); //$NON-NLS-1$

		// Add control
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fHostText = new Text(composite, SWT.BORDER);
		fHostText.setLayoutData(gridData);

		// Add label
		ctlLabel = new Label(composite, SWT.RIGHT);
		ctlLabel.setText(TelnetMessages.PORT + ":"); //$NON-NLS-1$

		// Add control
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fNetworkPortCombo = new Combo(composite, SWT.DROP_DOWN);

		fNetworkPortCombo.setLayoutData(gridData);

		List table = getNetworkPortMap().getNameTable();
		Collections.sort(table);
		loadCombo(fNetworkPortCombo, table);

		new Label(composite, SWT.RIGHT).setText(TelnetMessages.TIMEOUT + ":"); //$NON-NLS-1$
		fTimeout = new Text(composite, SWT.BORDER);
		fTimeout.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		loadSettings();
	}
	private void loadCombo(Combo ctlCombo, List table) {
		for (Iterator iter = table.iterator(); iter.hasNext();) {
			String label = (String) iter.next();
			ctlCombo.add(label);
		}
	}

}
