/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.serial.launcher;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.serial.SerialSettings;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.tm.terminal.view.ui.internal.SettingsStore;
import org.eclipse.tm.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.tm.terminal.view.ui.serial.controls.SerialConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.serial.nls.Messages;

/**
 * Serial launcher delegate implementation.
 */
@SuppressWarnings("restriction")
public class SerialLauncherDelegate extends AbstractLauncherDelegate {
	// The serial terminal connection memento handler
	private final IMementoHandler mementoHandler = new SerialMementoHandler();

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#needsUserConfiguration()
	 */
	@Override
	public boolean needsUserConfiguration() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#getPanel(org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer)
	 */
	@Override
	public IConfigurationPanel getPanel(IConfigurationPanelContainer container) {
		return new SerialConfigurationPanel(container);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#execute(java.util.Map, org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done)
	 */
	@Override
	public void execute(Map<String, Object> properties, ITerminalService.Done done) {
		Assert.isNotNull(properties);

		// Set the terminal tab title
		String terminalTitle = getTerminalTitle(properties);
		if (terminalTitle != null) {
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, terminalTitle);
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
	 * The default implementation constructs a title like &quot;Serial &lt;port&gt; (Start time) &quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	private String getTerminalTitle(Map<String, Object> properties) {
		String device = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE);

		if (device != null) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String date = format.format(new Date(System.currentTimeMillis()));
			return NLS.bind(Messages.SerialLauncherDelegate_terminalTitle, new String[]{device, date});
		}
		return Messages.SerialLauncherDelegate_terminalTitle_default;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (IMementoHandler.class.equals(adapter)) {
			return mementoHandler;
		}
	    return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#createTerminalConnector(java.util.Map)
	 */
    @Override
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
    	Assert.isNotNull(properties);

    	// Check for the terminal connector id
    	String connectorId = (String)properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null) connectorId = "org.eclipse.tm.internal.terminal.serial.SerialConnector"; //$NON-NLS-1$

		String port = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_DEVICE);
		String baud = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE);
		Object value = properties.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		String timeout = value instanceof Integer ? ((Integer)value).toString() : null;
		String databits = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS);
		String stopbits = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS);
		String parity = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_PARITY);
		String flowcontrol = (String)properties.get(ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL);

		// Construct the terminal settings store
		ISettingsStore store = new SettingsStore();

		// Construct the serial settings
		SerialSettings serialSettings = new SerialSettings();
		serialSettings.setSerialPort(port);
		serialSettings.setBaudRate(baud);
		serialSettings.setTimeout(timeout);
		serialSettings.setDataBits(databits);
		serialSettings.setStopBits(stopbits);
		serialSettings.setParity(parity);
		serialSettings.setFlowControl(flowcontrol);

		// And save the settings to the store
		serialSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		if (connector != null) {
			// Apply default settings
			connector.makeSettingsPage();
			// And load the real settings
			connector.load(store);
		}

		return connector;
	}
}
