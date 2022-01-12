/*******************************************************************************
 * Copyright (c) 2003, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Martin Oberhuber (Wind River) - [401476] Strip whitespace around Telnet Port
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;

public class TelnetSettingsPage extends AbstractSettingsPage {
	/* default */ Text fHostText;
	/* default */ Combo fNetworkPortCombo;
	/* default */ Text fTimeout;
	/* default */ Combo fEndOfLineCombo;
	private final TelnetSettings fTerminalSettings;

	public TelnetSettingsPage(TelnetSettings settings) {
		fTerminalSettings = settings;
	}

	@Override
	public void saveSettings() {
		fTerminalSettings.setHost(fHostText.getText());
		fTerminalSettings.setTimeout(fTimeout.getText());
		fTerminalSettings.setNetworkPort(getNetworkPort());
		fTerminalSettings.setEndOfLine(getEndOfLine());
	}

	@Override
	public void loadSettings() {
		if (fTerminalSettings != null) {
			setHost(fTerminalSettings.getHost());
			setTimeout(fTerminalSettings.getTimeoutString());
			setNetworkPort(fTerminalSettings.getNetworkPortString());
			setEndOfLine(fTerminalSettings.getEndOfLine());
		}
	}

	private void setHost(String strHost) {
		if (strHost == null)
			strHost = ""; //$NON-NLS-1$
		fHostText.setText(strHost);

	}

	private void setTimeout(String timeout) {
		if (timeout == null || timeout.length() == 0)
			timeout = "5"; //$NON-NLS-1$
		fTimeout.setText(timeout);

	}

	private void setNetworkPort(String strNetworkPort) {
		if (strNetworkPort != null) {
			String strPortName = getNetworkPortMap().findPortName(strNetworkPort);
			if (strPortName == null) {
				strPortName = strNetworkPort; //fallback to verbatim port if not found
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
		String portText = fNetworkPortCombo.getText().trim();
		String mappedPort = getNetworkPortMap().findPort(portText);
		return mappedPort != null ? mappedPort : portText;
	}

	private NetworkPortMap getNetworkPortMap() {
		return fTerminalSettings.getProperties().getNetworkPortMap();
	}

	private void setEndOfLine(String eol) {
		int idx = fEndOfLineCombo.indexOf(eol);
		fEndOfLineCombo.select(idx >= 0 ? idx : 0);
	}

	private String getEndOfLine() {
		return fEndOfLineCombo.getText();
	}

	@Override
	public boolean validateSettings() {
		String message = null;
		int messageType = IMessageProvider.NONE;
		boolean valid = true;

		if (fHostText.getText().trim().length() == 0) {
			String m = "Please enter a host IP or name."; //$NON-NLS-1$
			int mt = IMessageProvider.INFORMATION;
			updateControlDecoration(fHostText, m, mt);
			if (mt > messageType) {
				message = m;
				messageType = mt;
			}

			valid = false;
		} else {
			updateControlDecoration(fHostText, null, IMessageProvider.NONE);
		}

		try {
			int p = Integer.parseInt(getNetworkPort());
			if (p <= 0 || p > 65535) {
				String m = "Invalid network port. Must be between 0 and 65535."; //$NON-NLS-1$
				int mt = IMessageProvider.ERROR;
				updateControlDecoration(fNetworkPortCombo, m, mt);
				if (mt > messageType) {
					message = m;
					messageType = mt;
				}

				valid = false;
			} else {
				updateControlDecoration(fNetworkPortCombo, null, IMessageProvider.NONE);
			}

			p = Integer.parseInt(fTimeout.getText().trim());
			if (p < 0) {
				String m = "Invalid timeout. Must be greater than 0."; //$NON-NLS-1$
				int mt = IMessageProvider.ERROR;
				updateControlDecoration(fTimeout, m, mt);
				if (mt > messageType) {
					message = m;
					messageType = mt;
				}

				valid = false;
			} else {
				updateControlDecoration(fTimeout, null, IMessageProvider.NONE);
			}

		} catch (Exception e) {
			valid = false;
		}

		setMessage(message, messageType);
		return valid;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();

		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);

		// Add label
		Label ctlLabel = new Label(composite, SWT.RIGHT);
		ctlLabel.setText(TelnetMessages.HOST + ":"); //$NON-NLS-1$

		// Add control
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fHostText = new Text(composite, SWT.BORDER);
		fHostText.setLayoutData(gridData);
		fHostText.addModifyListener(e -> fireListeners(fHostText));
		createControlDecoration(fHostText);

		// Add label
		ctlLabel = new Label(composite, SWT.RIGHT);
		ctlLabel.setText(TelnetMessages.PORT + ":"); //$NON-NLS-1$

		// Add control
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fNetworkPortCombo = new Combo(composite, SWT.DROP_DOWN);
		fNetworkPortCombo.setLayoutData(gridData);
		fNetworkPortCombo.addModifyListener(e -> fireListeners(fNetworkPortCombo));
		fNetworkPortCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireListeners(fNetworkPortCombo);
			}
		});
		createControlDecoration(fNetworkPortCombo);

		List<String> table = getNetworkPortMap().getNameTable();
		Collections.sort(table);
		loadCombo(fNetworkPortCombo, table);

		new Label(composite, SWT.RIGHT).setText(TelnetMessages.TIMEOUT + ":"); //$NON-NLS-1$
		fTimeout = new Text(composite, SWT.BORDER);
		fTimeout.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fTimeout.addModifyListener(e -> fireListeners(fTimeout));
		createControlDecoration(fTimeout);

		new Label(composite, SWT.RIGHT).setText(TelnetMessages.END_OF_LINE + ":"); //$NON-NLS-1$
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		fEndOfLineCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fEndOfLineCombo.setLayoutData(gridData);
		fEndOfLineCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fireListeners(fEndOfLineCombo);
			}
		});
		loadCombo(fEndOfLineCombo, Arrays.asList(ITelnetSettings.EOL_CRNUL, ITelnetSettings.EOL_CRLF));

		loadSettings();
	}

	private void loadCombo(Combo ctlCombo, List<String> table) {
		for (Iterator<String> iter = table.iterator(); iter.hasNext();) {
			String label = iter.next();
			ctlCombo.add(label);
		}
	}

}
