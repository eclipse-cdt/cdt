/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [366374] [TERMINALS][TELNET] Add Telnet terminal support
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.controls;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.terminal.connector.telnet.connector.NetworkPortMap;
import org.eclipse.tm.terminal.connector.telnet.connector.TelnetConnector;
import org.eclipse.tm.terminal.connector.telnet.connector.TelnetSettings;
import org.eclipse.tm.terminal.connector.telnet.connector.TelnetSettingsPage;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;

/**
 * telnet wizard configuration panel implementation.
 */
public class TelnetWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

	public TelnetSettings telnetSettings;
	private ISettingsPage telnetSettingsPage;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public TelnetWizardConfigurationPanel(IConfigurationPanelContainer container) {
		super(container);
	}

	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		// Create the host selection combo
		if (isWithoutSelection())
			createHostsUI(panel, true);

		TelnetConnector conn = new TelnetConnector();
		telnetSettings = (TelnetSettings) conn.getTelnetSettings();
		telnetSettings.setHost(getSelectionHost());
		// MWE otherwise we don't get a valid default selection of the combo
		telnetSettings.setNetworkPort(NetworkPortMap.PROP_VALUETELNET);

		telnetSettingsPage = new TelnetSettingsPage(telnetSettings);
		if (telnetSettingsPage instanceof AbstractSettingsPage) {
			((AbstractSettingsPage) telnetSettingsPage).setHasControlDecoration(true);
		}
		telnetSettingsPage.createControl(panel);

		// Add the listener to the settings page
		telnetSettingsPage.addListener(control -> {
			if (getContainer() != null)
				getContainer().validate();
		});

		// Create the encoding selection combo
		createEncodingUI(panel, true);

		setControl(panel);
	}

	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null || telnetSettings == null || telnetSettingsPage == null)
			return;

		String value = (String) data.get(ITerminalsConnectorConstants.PROP_IP_HOST);
		if (value != null)
			telnetSettings.setHost(value);

		Object v = data.get(ITerminalsConnectorConstants.PROP_IP_PORT);
		value = v != null ? v.toString() : null;
		if (value != null)
			telnetSettings.setNetworkPort(value);

		v = data.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		value = v != null ? v.toString() : null;
		if (value != null)
			telnetSettings.setTimeout(value);

		v = data.get(ITerminalsConnectorConstants.PROP_TELNET_EOL);
		value = v != null ? v.toString() : null;
		if (value != null)
			telnetSettings.setEndOfLine(value);

		value = (String) data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (value != null)
			setEncoding(value);

		telnetSettingsPage.loadSettings();
	}

	@Override
	public void extractData(Map<String, Object> data) {
		if (data == null)
			return;

		// set the terminal connector id for ssh
		data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
				"org.eclipse.tm.terminal.connector.telnet.TelnetConnector"); //$NON-NLS-1$

		telnetSettingsPage.saveSettings();
		data.put(ITerminalsConnectorConstants.PROP_IP_HOST, telnetSettings.getHost());
		data.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.valueOf(telnetSettings.getNetworkPort()));
		data.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.valueOf(telnetSettings.getTimeout()));
		data.put(ITerminalsConnectorConstants.PROP_TELNET_EOL, telnetSettings.getEndOfLine());
		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
	}

	@Override
	protected void fillSettingsForHost(String host) {
		if (host != null && host.length() != 0) {
			if (hostSettingsMap.containsKey(host)) {
				Map<String, String> hostSettings = hostSettingsMap.get(host);
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST) != null) {
					telnetSettings.setHost(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT) != null) {
					telnetSettings.setNetworkPort(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT) != null) {
					telnetSettings.setTimeout(hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TELNET_EOL) != null) {
					telnetSettings.setEndOfLine(hostSettings.get(ITerminalsConnectorConstants.PROP_TELNET_EOL));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING) != null) {
					setEncoding(hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING));
				}
			} else {
				telnetSettings.setHost(getSelectionHost());
				// MWE otherwise we don't get a valid default selection of the combo
				telnetSettings.setNetworkPort(NetworkPortMap.PROP_VALUETELNET);
			}
			// set settings in page
			telnetSettingsPage.loadSettings();
		}
	}

	@Override
	protected void saveSettingsForHost(boolean add) {
		String host = getHostFromSettings();
		if (host != null && host.length() != 0) {
			Map<String, String> hostSettings = hostSettingsMap.get(host);
			if (hostSettings == null && add) {
				hostSettings = new HashMap<>();
				hostSettingsMap.put(host, hostSettings);
			}
			if (hostSettings != null) {
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, telnetSettings.getHost());
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT,
						Integer.toString(telnetSettings.getNetworkPort()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT,
						Integer.toString(telnetSettings.getTimeout()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_TELNET_EOL, telnetSettings.getEndOfLine());
				if (getEncoding() != null) {
					hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
				}
			}
		}
	}

	@Override
	public boolean isValid() {
		return isEncodingValid() && telnetSettingsPage.validateSettings();
	}

	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		saveSettingsForHost(true);
		super.doSaveWidgetValues(settings, idPrefix);
	}

	@Override
	protected String getHostFromSettings() {
		telnetSettingsPage.saveSettings();
		return telnetSettings.getHost();
	}
}
