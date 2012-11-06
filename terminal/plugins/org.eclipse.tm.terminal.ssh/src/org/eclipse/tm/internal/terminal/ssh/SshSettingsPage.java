/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 * Martin Oberhuber (Wind River) - [206917] Add validation for Terminal Settings
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.ssh;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;

public class SshSettingsPage implements ISettingsPage {
	private Text fHostText;
	private Text fUser;
	private Text fTimeout;
	private Text fKeepalive;
	private final SshSettings fTerminalSettings;
	private Text fPort;
	private Text fPassword;

	public SshSettingsPage(SshSettings settings) {
		fTerminalSettings=settings;
	}
	public void saveSettings() {
		fTerminalSettings.setHost(fHostText.getText());
		fTerminalSettings.setUser(fUser.getText());
		fTerminalSettings.setPassword(fPassword.getText());
		fTerminalSettings.setPort(fPort.getText());
		fTerminalSettings.setTimeout(fTimeout.getText());
		fTerminalSettings.setKeepalive(fKeepalive.getText());
	}

	public void loadSettings() {
		if(fTerminalSettings!=null) {
			fHostText.setText(get(fTerminalSettings.getHost(),""));//$NON-NLS-1$
			fTimeout.setText(get(fTerminalSettings.getTimeoutString(),"0"));//$NON-NLS-1$
			fKeepalive.setText(get(fTerminalSettings.getKeepaliveString(),"300"));//$NON-NLS-1$
			fUser.setText(get(fTerminalSettings.getUser(),""));//$NON-NLS-1$
			fPort.setText(get(fTerminalSettings.getPortString(),"22"));//$NON-NLS-1$
			fPassword.setText(get(fTerminalSettings.getPassword(),""));//$NON-NLS-1$
		}
	}
	String get(String value, String def) {
		if(value==null || value.length()==0)
			return def;
		return value;
	}
	public boolean validateSettings() {
		if (fHostText.getText().trim().length() == 0) {
			return false;
		}
		if (fUser.getText().trim().length() == 0) {
			return false;
		}
		try {
			int p = Integer.parseInt(fPort.getText().trim());
			if (p <= 0 || p > 65535) {
				return false;
			}
			p = Integer.parseInt(fTimeout.getText().trim());
			if (p < 0) {
				return false;
			}
			p = Integer.parseInt(fKeepalive.getText().trim());
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

		fHostText = createTextField(composite, SshMessages.HOST);
		fUser = createTextField(composite, SshMessages.USER);
		fPassword = createTextField(composite, SshMessages.PASSWORD,SWT.PASSWORD);
		fTimeout = createTextField(composite, SshMessages.TIMEOUT);
		fKeepalive = createTextField(composite, SshMessages.KEEPALIVE);
		fKeepalive.setToolTipText(SshMessages.KEEPALIVE_Tooltip);
		fPort = createTextField(composite, SshMessages.PORT);
		loadSettings();
	}
	private Text createTextField(Composite composite, String labelTxt, int textOptions) {
		GridData gridData;
		// Add label
		Label ctlLabel = new Label(composite, SWT.RIGHT);
		ctlLabel.setText(labelTxt + ":"); //$NON-NLS-1$

		// Add control
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		Text text= new Text(composite, SWT.BORDER | textOptions);
		text.setLayoutData(gridData);
		return text;
	}
	private Text createTextField(Composite composite, String labelTxt) {
		return createTextField(composite, labelTxt, 0);
	}

}
