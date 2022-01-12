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
package org.eclipse.tm.terminal.connector.telnet.launcher;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.terminal.connector.telnet.connector.TelnetSettings;
import org.eclipse.tm.terminal.connector.telnet.controls.TelnetWizardConfigurationPanel;
import org.eclipse.tm.terminal.connector.telnet.nls.Messages;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;
import org.eclipse.tm.terminal.view.ui.launcher.AbstractLauncherDelegate;

/**
 * Telnet launcher delegate implementation.
 */
@SuppressWarnings("restriction")
public class TelnetLauncherDelegate extends AbstractLauncherDelegate {
	// The Telnet terminal connection memento handler
	private final IMementoHandler mementoHandler = new TelnetMementoHandler();

	@Override
	public boolean needsUserConfiguration() {
		return true;
	}

	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new TelnetWizardConfigurationPanel(container);
	}

	@Override
	public void execute(Map<String, Object> properties, ITerminalService.Done done) {
		Assert.isNotNull(properties);

		// Set the terminal tab title
		String terminalTitle = getTerminalTitle(properties);
		if (terminalTitle != null) {
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, terminalTitle);
		}

		// For Telnet terminals, force a new terminal tab each time it is launched,
		// if not set otherwise from outside
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW)) {
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}

		// Get the terminal service
		ITerminalService terminal = TerminalServiceFactory.getService();
		// If not available, we cannot fulfill this request
		if (terminal != null) {
			terminal.openConsole(properties, done);
		}
	}

	/**
	 * Returns the terminal title string.
	 * <p>
	 * The default implementation constructs a title like &quot;Telnet @ host (Start time) &quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	private String getTerminalTitle(Map<String, Object> properties) {
		// Try to see if the user set a title explicitly via the properties map.
		String title = getDefaultTerminalTitle(properties);
		if (title != null)
			return title;

		//No title,try to calculate the title
		String host = (String) properties.get(ITerminalsConnectorConstants.PROP_IP_HOST);

		if (host != null) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String date = format.format(new Date(System.currentTimeMillis()));
			return NLS.bind(Messages.TelnetLauncherDelegate_terminalTitle, new String[] { host, date });
		}

		return Messages.TelnetLauncherDelegate_terminalTitle_default;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IMementoHandler.class.equals(adapter)) {
			return adapter.cast(mementoHandler);
		}
		return super.getAdapter(adapter);
	}

	@Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null)
			connectorId = "org.eclipse.tm.terminal.connector.telnet.TelnetConnector"; //$NON-NLS-1$

		// Extract the telnet properties
		String host = (String) properties.get(ITerminalsConnectorConstants.PROP_IP_HOST);
		Object value = properties.get(ITerminalsConnectorConstants.PROP_IP_PORT);
		String port = value != null ? value.toString() : null;
		value = properties.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		String timeout = value != null ? value.toString() : null;
		String endOfLine = (String) properties.get(ITerminalsConnectorConstants.PROP_TELNET_EOL);

		int portOffset = 0;
		if (properties.get(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET) instanceof Integer) {
			portOffset = ((Integer) properties.get(ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET)).intValue();
			if (portOffset < 0)
				portOffset = 0;
		}

		// The real port to connect to is port + portOffset
		if (port != null) {
			port = Integer.toString(Integer.decode(port).intValue() + portOffset);
		}

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the telnet settings
		TelnetSettings telnetSettings = new TelnetSettings();
		telnetSettings.setHost(host);
		telnetSettings.setNetworkPort(port);
		if (timeout != null) {
			telnetSettings.setTimeout(timeout);
		}
		telnetSettings.setEndOfLine(endOfLine);
		// And save the settings to the store
		telnetSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			// Apply default settings
			connector.setDefaultSettings();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}
}
