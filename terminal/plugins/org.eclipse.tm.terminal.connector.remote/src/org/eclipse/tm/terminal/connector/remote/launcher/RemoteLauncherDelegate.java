/*******************************************************************************
 * Copyright (c) 2015,2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.launcher;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.terminal.connector.ISettingsStore;
import org.eclipse.terminal.connector.ITerminalConnector;
import org.eclipse.terminal.connector.InMemorySettingsStore;
import org.eclipse.terminal.connector.TerminalConnectorExtension;
import org.eclipse.terminal.view.core.ITerminalService;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.IMementoHandler;
import org.eclipse.terminal.view.ui.launcher.AbstractLauncherDelegate;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.connector.remote.IRemoteSettings;
import org.eclipse.tm.terminal.connector.remote.controls.RemoteWizardConfigurationPanel;
import org.eclipse.tm.terminal.connector.remote.internal.Activator;
import org.eclipse.tm.terminal.connector.remote.internal.RemoteSettings;
import org.eclipse.tm.terminal.connector.remote.nls.Messages;

/**
 * Remote launcher delegate implementation.
 */
public class RemoteLauncherDelegate extends AbstractLauncherDelegate {
	// The Remote terminal connection memento handler
	private final IMementoHandler mementoHandler = new RemoteMementoHandler();

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
		return new RemoteWizardConfigurationPanel(container);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate#execute(java.util.Map, org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done)
	 */
	@Override
	public CompletableFuture<?> execute(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// Set the terminal tab title
		String terminalTitle = getTerminalTitle(properties);
		if (terminalTitle != null) {
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, terminalTitle);
		}

		// Force a new terminal tab each time it is launched, if not set otherwise from outside
		// TODO need a command shell service routing to get this
		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW)) {
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, Boolean.TRUE);
		}

		// Get the terminal service
		ITerminalService terminal = getTerminalService();
		// If not available, we cannot fulfill this request
		return terminal.openConsole(properties);
	}

	/**
	 * Returns the terminal title string.
	 * <p>
	 * The default implementation constructs a title like &quot;SSH @ host (Start time) &quot;.
	 *
	 * @return The terminal title string or <code>null</code>.
	 */
	private String getTerminalTitle(Map<String, Object> properties) {
		// Try to see if the user set a title explicitly via the properties map.
		String title = getDefaultTerminalTitle(properties);
		if (title != null)
			return title;

		String connection = (String) properties.get(IRemoteSettings.CONNECTION_NAME);

		if (connection != null) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String date = format.format(new Date(System.currentTimeMillis()));
			return NLS.bind(Messages.RemoteLauncherDelegate_terminalTitle, new String[] { connection, date });
		}

		return Messages.RemoteLauncherDelegate_terminalTitle_default;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
	public ITerminalConnector createTerminalConnector(Map<String, Object> properties) throws CoreException {
		Assert.isNotNull(properties);

		// Check for the terminal connector id
		String connectorId = (String) properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
		if (connectorId == null) {
			connectorId = "org.eclipse.tm.terminal.connector.remote.RemoteConnector"; //$NON-NLS-1$
		}

		// Extract the remote properties
		String connTypeId = (String) properties.get(IRemoteSettings.CONNECTION_TYPE_ID);
		String connName = (String) properties.get(IRemoteSettings.CONNECTION_NAME);

		// Construct the terminal settings store
		ISettingsStore store = new InMemorySettingsStore();

		// Construct the remote settings
		RemoteSettings remoteSettings = new RemoteSettings();
		remoteSettings.setConnectionTypeId(connTypeId);
		remoteSettings.setConnectionName(connName);
		// And save the settings to the store
		remoteSettings.save(store);

		// Construct the terminal connector instance
		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
		// Apply default settings
		connector.setDefaultSettings();
		// And load the real settings
		connector.load(store);

		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_ENCODING)) {
			IRemoteServicesManager svcMgr = Activator.getService(IRemoteServicesManager.class);

			IRemoteConnectionType connType = svcMgr.getConnectionType(connTypeId);
			if (connType != null) {
				IRemoteConnection remoteConnection = connType.getConnection(connName);
				if (remoteConnection != null && remoteConnection.isOpen()) {
					properties.put(ITerminalsConnectorConstants.PROP_ENCODING,
							remoteConnection.getProperty(IRemoteConnection.LOCALE_CHARMAP_PROPERTY));
				}
			}
		}

		properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, "/tmp"); //$NON-NLS-1$

		return connector;
	}

}
