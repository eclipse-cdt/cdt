/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote.controls;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.terminal.connector.remote.IRemoteSettings;
import org.eclipse.tm.terminal.connector.remote.internal.RemoteConnector;
import org.eclipse.tm.terminal.connector.remote.internal.RemoteSettings;
import org.eclipse.tm.terminal.connector.remote.internal.RemoteSettingsPage;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;

/**
 * Remote wizard configuration panel implementation.
 */
@SuppressWarnings("restriction")
public class RemoteWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

    public RemoteSettings remoteSettings;
	private ISettingsPage remoteSettingsPage;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public RemoteWizardConfigurationPanel(IConfigurationPanelContainer container) {
	    super(container);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		RemoteConnector conn = new RemoteConnector();
		remoteSettings = (RemoteSettings) conn.getRemoteSettings();

		remoteSettingsPage = new RemoteSettingsPage(remoteSettings);
		if (remoteSettingsPage instanceof AbstractSettingsPage) {
			((AbstractSettingsPage)remoteSettingsPage).setHasControlDecoration(true);
		}
		remoteSettingsPage.createControl(panel);

		// Add the listener to the settings page
		remoteSettingsPage.addListener(new ISettingsPage.Listener() {

			@Override
			public void onSettingsPageChanged(Control control) {
				if (getContainer() != null) getContainer().validate();
			}
		});

		// Create the encoding selection combo
		createEncodingUI(panel, true);

		setControl(panel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#setupData(java.util.Map)
	 */
	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null || remoteSettings == null || remoteSettingsPage == null) return;

		String value = (String)data.get(IRemoteSettings.REMOTE_SERVICES);
		if (value != null) remoteSettings.setRemoteServices(value);

		value = (String)data.get(IRemoteSettings.CONNECTION_NAME);
		if (value != null) remoteSettings.setConnectionName(value);

		value = (String)data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (value != null) setEncoding(value);

		remoteSettingsPage.loadSettings();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#extractData(java.util.Map)
	 */
	@Override
	public void extractData(Map<String, Object> data) {
		if (data == null) return;

    	// set the terminal connector id for remote
    	data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.terminal.connector.remote.RemoteConnector"); //$NON-NLS-1$

    	remoteSettingsPage.saveSettings();
    	
    	data.put(IRemoteSettings.REMOTE_SERVICES, remoteSettings.getRemoteServices());
    	data.put(IRemoteSettings.CONNECTION_NAME, remoteSettings.getConnectionName());
		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#fillSettingsForHost(java.lang.String)
	 */
	@Override
	protected void fillSettingsForHost(String host){
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#saveSettingsForHost(boolean)
	 */
	@Override
	protected void saveSettingsForHost(boolean add){
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#isValid()
	 */
	@Override
    public boolean isValid(){
		return isEncodingValid() && remoteSettingsPage.validateSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
    public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
    	saveSettingsForHost(true);
    	super.doSaveWidgetValues(settings, idPrefix);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#getHostFromSettings()
	 */
	@Override
    protected String getHostFromSettings() {
		remoteSettingsPage.saveSettings();
	    return null;
    }
}
