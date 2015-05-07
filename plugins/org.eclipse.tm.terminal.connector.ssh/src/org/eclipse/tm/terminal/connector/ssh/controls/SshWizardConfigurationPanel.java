/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.controls;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.terminal.connector.ssh.connector.SshConnector;
import org.eclipse.tm.terminal.connector.ssh.connector.SshSettings;
import org.eclipse.tm.terminal.connector.ssh.connector.SshSettingsPage;
import org.eclipse.tm.terminal.connector.ssh.nls.Messages;
import org.eclipse.tm.terminal.view.core.TerminalContextPropertiesProviderFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalContextPropertiesProvider;
import org.eclipse.tm.terminal.view.core.interfaces.constants.IContextPropertiesConstants;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;
import org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel;

/**
 * SSH wizard configuration panel implementation.
 */
public class SshWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

	private static final String SAVE_USER = "saveUser"; //$NON-NLS-1$
	private static final String SAVE_PASSWORD = "savePassword"; //$NON-NLS-1$

    private SshSettings sshSettings;
	private ISettingsPage sshSettingsPage;
	private Button userButton;
	private Button passwordButton;

	/**
	 * Constructor.
	 *
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public SshWizardConfigurationPanel(IConfigurationPanelContainer container) {
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

		// Create the host selection combo
		if (isWithoutSelection()) createHostsUI(panel, true);

		SshConnector conn = new SshConnector();
		sshSettings = (SshSettings) conn.getSshSettings();
		sshSettings.setHost(getSelectionHost());
		sshSettings.setUser(getDefaultUser());

		sshSettingsPage = new SshSettingsPage(sshSettings);
		if (sshSettingsPage instanceof AbstractSettingsPage) {
			((AbstractSettingsPage)sshSettingsPage).setHasControlDecoration(true);
		}
		sshSettingsPage.createControl(panel);

		// Add the listener to the settings page
		sshSettingsPage.addListener(new ISettingsPage.Listener() {
			@Override
			public void onSettingsPageChanged(Control control) {
				if (getContainer() != null) getContainer().validate();
			}
		});

		// Create the encoding selection combo
		createEncodingUI(panel, true);

		// if user and password for host should be saved or not
		createSaveButtonsUI(panel, true);

		setControl(panel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#setupData(java.util.Map)
	 */
	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null || sshSettings == null || sshSettingsPage == null) return;

		String value = (String)data.get(ITerminalsConnectorConstants.PROP_IP_HOST);
		if (value != null) sshSettings.setHost(value);

		Object v = data.get(ITerminalsConnectorConstants.PROP_IP_PORT);
		value = v != null ? v.toString() : null;
		if (value != null) sshSettings.setPort(value);

		v = data.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		value = v != null ? v.toString() : null;
		if (value != null) sshSettings.setTimeout(value);

		v = data.get(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE);
		value = v != null ? v.toString() : null;
		if (value != null) sshSettings.setKeepalive(value);

		value = (String)data.get(ITerminalsConnectorConstants.PROP_SSH_PASSWORD);
		if (value != null) sshSettings.setPassword(value);

		value = (String)data.get(ITerminalsConnectorConstants.PROP_SSH_USER);
		if (value != null) sshSettings.setUser(value);

		value = (String)data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (value != null) setEncoding(value);

		sshSettingsPage.loadSettings();
    }

	/**
	 * Returns the default user name.
	 *
	 * @return The default user name.
	 */
	private String getDefaultUser() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			ITerminalContextPropertiesProvider provider = TerminalContextPropertiesProviderFactory.getProvider(element);
			if (provider != null) {
				Object user = provider.getProperty(element, IContextPropertiesConstants.PROP_DEFAULT_USER);
				if (user instanceof String) return ((String) user).trim();
			}
		}

		return System.getProperty("user.name"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#extractData(java.util.Map)
	 */
	@Override
	public void extractData(Map<String, Object> data) {
		if (data == null) return;

    	// set the terminal connector id for ssh
    	data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.terminal.connector.ssh.SshConnector"); //$NON-NLS-1$

    	sshSettingsPage.saveSettings();
		data.put(ITerminalsConnectorConstants.PROP_IP_HOST,sshSettings.getHost());
		data.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.valueOf(sshSettings.getPort()));
		data.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.valueOf(sshSettings.getTimeout()));
		data.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.valueOf(sshSettings.getKeepalive()));
		data.put(ITerminalsConnectorConstants.PROP_SSH_PASSWORD, sshSettings.getPassword());
		data.put(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#fillSettingsForHost(java.lang.String)
	 */
	@Override
	protected void fillSettingsForHost(String host) {
		boolean saveUser = true;
		boolean savePassword = false;
		if (host != null && host.length() != 0){
			if (hostSettingsMap.containsKey(host)){
				Map<String, String> hostSettings = hostSettingsMap.get(host);
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST) != null) {
					sshSettings.setHost(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_HOST));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT) != null) {
					sshSettings.setPort(hostSettings.get(ITerminalsConnectorConstants.PROP_IP_PORT));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT) != null) {
					sshSettings.setTimeout(hostSettings.get(ITerminalsConnectorConstants.PROP_TIMEOUT));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE) != null) {
					sshSettings.setKeepalive(hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE));
				}
				if (hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_USER) != null) {
					sshSettings.setUser(hostSettings.get(ITerminalsConnectorConstants.PROP_SSH_USER));
				}
				if (hostSettings.get(SAVE_PASSWORD) != null) {
					savePassword = new Boolean(hostSettings.get(SAVE_PASSWORD)).booleanValue();
				}
				if (!savePassword){
					sshSettings.setPassword(""); //$NON-NLS-1$
				} else {
					String password = accessSecurePassword(sshSettings.getHost());
					if (password != null) {
						sshSettings.setPassword(password);
					}
				}

				String encoding = hostSettings.get(ITerminalsConnectorConstants.PROP_ENCODING);
				if (encoding == null || "null".equals(encoding)) { //$NON-NLS-1$
					String defaultEncoding = getSelectionEncoding();
					encoding = defaultEncoding != null && !"".equals(defaultEncoding.trim()) ? defaultEncoding.trim() : "ISO-8859-1"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				setEncoding(encoding);
			} else {
				sshSettings.setHost(getSelectionHost());
				sshSettings.setUser(getDefaultUser());
				saveUser = true;
				savePassword = false;
			}
			// set settings in page
			sshSettingsPage.loadSettings();
			userButton.setSelection(saveUser);
			passwordButton.setSelection(savePassword);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractExtendedConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
    public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
    	saveSettingsForHost(true);
    	super.doSaveWidgetValues(settings, idPrefix);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#saveSettingsForHost(boolean)
	 */
	@Override
	protected void saveSettingsForHost(boolean add) {
		boolean saveUser = userButton.getSelection();
		boolean savePassword = passwordButton.getSelection();
		String host = getHostFromSettings();
		if (host != null && host.length() != 0) {
			if (hostSettingsMap.containsKey(host)){
				Map<String, String> hostSettings = hostSettingsMap.get(host);
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, sshSettings.getHost());
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(sshSettings.getPort()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(sshSettings.getTimeout()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.toString(sshSettings.getKeepalive()));
				if (saveUser) {
					String defaultUser = getDefaultUser();
					if (defaultUser == null || !defaultUser.equals(sshSettings.getUser())) {
						hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
					} else {
						hostSettings.remove(ITerminalsConnectorConstants.PROP_SSH_USER);
					}
				}
				else {
					hostSettings.remove(ITerminalsConnectorConstants.PROP_SSH_USER);
				}

				String encoding = getEncoding();
				if (encoding != null) {
					String defaultEncoding = getSelectionEncoding();
					if (defaultEncoding != null && defaultEncoding.trim().equals(encoding)) {
						encoding = null;
					}
				}
				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, encoding);
				hostSettings.put(SAVE_USER, Boolean.toString(saveUser));
				hostSettings.put(SAVE_PASSWORD, Boolean.toString(savePassword));

				if (savePassword && sshSettings.getPassword() != null && sshSettings.getPassword().length() != 0){
					saveSecurePassword(host, sshSettings.getPassword());
				}

				// maybe unchecked the password button - so try to remove a saved password - if any
				if (!savePassword) removeSecurePassword(host);
			} else if (add) {
				Map<String, String> hostSettings = new HashMap<String, String>();
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_HOST, sshSettings.getHost());
				hostSettings.put(ITerminalsConnectorConstants.PROP_IP_PORT, Integer.toString(sshSettings.getPort()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_TIMEOUT, Integer.toString(sshSettings.getTimeout()));
				hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, Integer.toString(sshSettings.getKeepalive()));
				if (saveUser) {
					String defaultUser = getDefaultUser();
					if (defaultUser == null || !defaultUser.equals(sshSettings.getUser())) {
						hostSettings.put(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
					}
				}
				hostSettings.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());
				hostSettings.put(SAVE_USER, Boolean.toString(saveUser));
				hostSettings.put(SAVE_PASSWORD, Boolean.toString(savePassword));
				hostSettingsMap.put(host, hostSettings);

				if (savePassword && sshSettings.getPassword() != null && sshSettings.getPassword().length() != 0){
					saveSecurePassword(host, sshSettings.getPassword());
				}
			}
		}
	}

	/**
	 * Save the password to the secure storage.
	 *
	 * @param host The host. Must not be <code>null</code>.
	 * @param password The password. Must not be <code>null</code>.
	 */
	private void saveSecurePassword(String host, String password) {
		Assert.isNotNull(host);
		Assert.isNotNull(password);

		// To access the secure storage, we need the preference instance
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences != null) {
			// Construct the secure preferences node key
			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
			ISecurePreferences node = preferences.node(nodeKey);
			if (node != null) {
				try {
					node.put("password", password, true); //$NON-NLS-1$
				}
				catch (StorageException ex) { /* ignored on purpose */ }
			}
		}
	}

	/**
	 * Reads the password from the secure storage.
	 *
	 * @param host The host. Must not be <code>null</code>.
	 * @return The password or <code>null</code>.
	 */
	private String accessSecurePassword(String host) {
		Assert.isNotNull(host);

		// To access the secure storage, we need the preference instance
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences != null) {
			// Construct the secure preferences node key
			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
			ISecurePreferences node = preferences.node(nodeKey);
			if (node != null) {
				String password = null;
				try {
					password = node.get("password", null); //$NON-NLS-1$
				}
				catch (StorageException ex) { /* ignored on purpose */ }

				return password;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#removeSecurePassword(java.lang.String)
	 */
	@Override
	protected void removeSecurePassword(String host) {
		Assert.isNotNull(host);

		// To access the secure storage, we need the preference instance
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences != null) {
			// Construct the secure preferences node key
			String nodeKey = "/Target Explorer SSH Password/" + host; //$NON-NLS-1$
			ISecurePreferences node = preferences.node(nodeKey);
			if (node != null) {
				node.remove("password"); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#isValid()
	 */
	@Override
    public boolean isValid(){
		return isEncodingValid() && sshSettingsPage.validateSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.ui.panels.AbstractConfigurationPanel#getHostFromSettings()
	 */
	@Override
    protected String getHostFromSettings() {
		sshSettingsPage.saveSettings();
	    return sshSettings.getHost();
    }

	private void createSaveButtonsUI(final Composite parent, boolean separator) {
		Assert.isNotNull(parent);

		if (separator) {
			Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		userButton = new Button(panel, SWT.CHECK);
		userButton.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		userButton.setText(Messages.SshWizardConfigurationPanel_saveUser);

		passwordButton = new Button(panel, SWT.CHECK);
		passwordButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		passwordButton.setText(Messages.SshWizardConfigurationPanel_savePassword);
	}
}
