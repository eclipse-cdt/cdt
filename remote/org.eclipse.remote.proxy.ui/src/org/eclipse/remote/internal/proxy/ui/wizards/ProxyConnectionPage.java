/**
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.remote.internal.proxy.ui.wizards;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.proxy.core.ProxyConnection;
import org.eclipse.remote.internal.proxy.ui.Activator;
import org.eclipse.remote.internal.proxy.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class ProxyConnectionPage extends WizardPage {

	private class DataModifyListener implements ModifyListener {
		@Override
		public synchronized void modifyText(ModifyEvent e) {
			validateFields();
			getContainer().updateButtons();
		}
	}

	private Text fConnectionName;
	private Button fPasswordButton;
	private Button fPublicKeyButton;
	private Button fDefaultServerButton;
	private Text fHostText;
	private Text fUserText;
	private Text fPasswordText;
	private Text fPassphraseText;
	private Text fPortText;
	private Text fTimeoutText;
	private Text fServerCommandText;

	private String fInitialName = "Remote Host"; //$NON-NLS-1$
	private Set<String> fInvalidConnectionNames;
	private final Map<String, String> fInitialAttributes = new HashMap<String, String>();
	private IRemoteConnectionWorkingCopy fConnection;

	private final IRemoteConnectionType fConnectionType;

	private final DataModifyListener fDataModifyListener = new DataModifyListener();

	public ProxyConnectionPage(IRemoteConnectionType connectionType) {
		super(Messages.ProxyNewConnectionPage_New_connection_properties);
		fConnectionType = connectionType;
		setPageComplete(false);
	}

	/**
	 * Create controls for the bottom (hideable) advanced composite
	 *
	 * @param mold
	 *
	 */
	private void createAdvancedControls(final Composite parent) {
		ExpandableComposite expComp = new ExpandableComposite(parent, ExpandableComposite.TWISTIE);
		expComp.setText(Messages.ProxyNewConnectionPage_Advanced);
		expComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		expComp.setExpanded(false);
		expComp.addExpansionListener(new IExpansionListener() {

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				for (int i = 0; i < 2; i++) { // sometimes the size compute isn't correct on first try
					Point newSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					Point currentSize = parent.getSize();
					int deltaY = newSize.y - currentSize.y;
					Point shellSize = getShell().getSize();
					shellSize.y += deltaY;
					getShell().setSize(shellSize);
					getShell().layout(true, true);
				}
			}

			@Override
			public void expansionStateChanging(ExpansionEvent e) {
				// Ignore
			}
		});

		Composite advancedComp = new Composite(expComp, SWT.NONE);
		advancedComp.setLayout(new GridLayout(1, false));
		advancedComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Group settingsComp = new Group(advancedComp, SWT.NONE);
		settingsComp.setText(Messages.ProxyConnectionPage_Settings0);
		settingsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		settingsComp.setLayout(new GridLayout(2, false));

		Label portLabel = new Label(settingsComp, SWT.NONE);
		portLabel.setText(Messages.ProxyNewConnectionPage_Port);
		fPortText = new Text(settingsComp, SWT.BORDER | SWT.SINGLE);
		fPortText.setText(Integer.toString(ProxyConnection.DEFAULT_PORT));
		fPortText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		setTextFieldWidthInChars(fPortText, 5);

		Label timeoutLabel = new Label(settingsComp, SWT.NONE);
		timeoutLabel.setText(Messages.ProxyNewConnectionPage_Timeout);
		fTimeoutText = new Text(settingsComp, SWT.BORDER | SWT.SINGLE);
		fTimeoutText.setText(Integer.toString(ProxyConnection.DEFAULT_TIMEOUT));

		fDefaultServerButton = new Button(settingsComp, SWT.CHECK);
		fDefaultServerButton.setText("Use default server");
		fDefaultServerButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		fDefaultServerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateFields();
				updateEnablement();
			}
		});
		Label serverLabel = new Label(settingsComp, SWT.NONE);
		serverLabel.setText("Server command");
		fServerCommandText = new Text(settingsComp, SWT.BORDER | SWT.SINGLE);
		fServerCommandText.setText(ProxyConnection.DEFAULT_SERVER_COMMAND);
		fServerCommandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fDefaultServerButton.setSelection(ProxyConnection.DEFAULT_USE_DEFAULT_SERVER);

		expComp.setClient(advancedComp);
	}

	private void createAuthControls(Composite parent) {
		Composite controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new GridLayout(3, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label hostLabel = new Label(controls, SWT.NONE);
		hostLabel.setText(Messages.ProxyNewConnectionPage_Host);
		fHostText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label userLabel = new Label(controls, SWT.NONE);
		userLabel.setText(Messages.ProxyNewConnectionPage_User);
		fUserText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fUserText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// Key option box
		fPublicKeyButton = new Button(controls, SWT.RADIO);
		fPublicKeyButton.setText(Messages.ProxyNewConnectionPage_Public_key_based_authentication);
		fPublicKeyButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		fPublicKeyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateFields();
				updateEnablement();
			}
		});

		Link link = new Link(controls, SWT.WRAP);
		final GridData linkLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		link.setLayoutData(linkLayoutData);
		final String PREFS_PAGE_ID_NET_SSH = "org.eclipse.jsch.ui.SSHPreferences"; //$NON-NLS-1$
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(getShell(), PREFS_PAGE_ID_NET_SSH,
						new String[] { PREFS_PAGE_ID_NET_SSH }, null);
				dlg.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});
		link.setText(Messages.ProxyConnectionPage_KeysAtSSH2);

		// Passphrase field
		Label passphraseLabel = new Label(controls, SWT.NONE);
		passphraseLabel.setText(Messages.ProxyNewConnectionPage_Passphrase);
		fPassphraseText = new Text(controls, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		fPassphraseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// User option box
		fPasswordButton = new Button(controls, SWT.RADIO);
		fPasswordButton.setText(Messages.ProxyNewConnectionPage_Password_based_authentication);
		fPasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		fPasswordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateFields();
				updateEnablement();
			}
		});

		// Password field
		Label passwordLabel = new Label(controls, SWT.NONE);
		passwordLabel.setText(Messages.ProxyNewConnectionPage_Password);
		fPasswordText = new Text(controls, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		fPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		fPasswordButton.setSelection(ProxyConnection.DEFAULT_USE_PASSWORD);
		fPublicKeyButton.setSelection(!ProxyConnection.DEFAULT_USE_PASSWORD);
		controls.setTabList(new Control[] { fHostText, fUserText, fPublicKeyButton, fPassphraseText, fPasswordButton,
				fPasswordText });
	}

	@Override
	public void createControl(Composite parent) {
		if (fConnection == null) {
			setDescription(Messages.ProxyNewConnectionPage_New_connection_properties);
			setTitle(Messages.ProxyNewConnectionPage_New_connection_properties);
			setMessage(Messages.ProxyConnectionPage_Initial_Message);
		} else {
			setDescription(Messages.ProxyConnectionPage_Edit_properties_of_an_existing_connection);
			setTitle(Messages.ProxyConnectionPage_Edit_Connection);
		}
		setErrorMessage(null);

		GridLayout topLayout = new GridLayout(2, false);
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);

		Label label = new Label(topControl, SWT.NONE);
		label.setText(Messages.ProxyNewConnectionPage_Connection_name);

		fConnectionName = new Text(topControl, SWT.BORDER | SWT.SINGLE);
		fConnectionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fConnectionName.setEnabled(fConnection == null);

		final Group authGroup = new Group(topControl, SWT.NONE);
		authGroup.setText(Messages.ProxyNewConnectionPage_Host_information);
		authGroup.setLayout(new GridLayout(1, false));
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		createAuthControls(authGroup);
		createAdvancedControls(authGroup);

		try {
			loadValues();
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		/*
		 * Register listeners after loading values so we don't trigger listeners
		 */
		registerListeners();

		if (fConnection != null) {
			validateFields();
		}

		updateEnablement();
	}

	public IRemoteConnectionWorkingCopy getConnection() {
		return fConnection;
	}

	/**
	 * Check if the connection name is invalid. This only applies to new connections (when fConnection is null).
	 *
	 * @param name
	 *            connection name
	 * @return true if the name is invalid, false otherwise
	 */
	private boolean isInvalidName(String name) {
		if (fConnection == null) {
			if (fInvalidConnectionNames == null) {
				return fConnectionType.getConnection(name) != null;
			}
			return fInvalidConnectionNames.contains(name);
		}
		return false;
	}

	private void loadValues() throws CoreException {
		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		if (fConnection != null) {
			fConnectionName.setText(fConnection.getName());
			fHostText.setText(fConnection.getAttribute(ProxyConnection.HOSTNAME_ATTR));
			fUserText.setText(fConnection.getAttribute(ProxyConnection.USERNAME_ATTR));
			String portStr = fConnection.getAttribute(ProxyConnection.PORT_ATTR);
			fPortText.setText(portStr.isEmpty() ? Integer.toString(ProxyConnection.DEFAULT_PORT) : portStr);
			String timeoutStr = fConnection.getAttribute(ProxyConnection.TIMEOUT_ATTR);
			fTimeoutText.setText(timeoutStr.isEmpty() ? Integer.toString(ProxyConnection.DEFAULT_TIMEOUT) : timeoutStr);
			String isPwdStr = fConnection.getAttribute(ProxyConnection.USE_PASSWORD_ATTR);
			boolean isPwd = isPwdStr.isEmpty() ? ProxyConnection.DEFAULT_USE_PASSWORD : Boolean.parseBoolean(isPwdStr);
			fPasswordButton.setSelection(isPwd);
			fPublicKeyButton.setSelection(!isPwd);
			fPasswordText.setText(fConnection.getSecureAttribute(ProxyConnection.PASSWORD_ATTR));
			fPassphraseText.setText(fConnection.getSecureAttribute(ProxyConnection.PASSPHRASE_ATTR));
			String useDefaultServerStr = fConnection.getAttribute(ProxyConnection.USE_DEFAULT_SERVER_ATTR);
			boolean useDefaultServer = useDefaultServerStr.isEmpty() ? ProxyConnection.DEFAULT_USE_DEFAULT_SERVER
					: Boolean.parseBoolean(useDefaultServerStr);
			fDefaultServerButton.setSelection(useDefaultServer);
			String serverCommandStr = fConnection.getAttribute(ProxyConnection.SERVER_COMMAND_ATTR);
			fServerCommandText
					.setText(serverCommandStr.isEmpty() ? ProxyConnection.DEFAULT_SERVER_COMMAND : serverCommandStr);

		} else {
			fConnectionName.setText(fInitialName);
			String host = fInitialAttributes.get(ProxyConnection.HOSTNAME_ATTR);
			if (host != null) {
				fHostText.setText(host);
			}
			String username = fInitialAttributes.get(ProxyConnection.USERNAME_ATTR);
			if (username != null) {
				fUserText.setText(username);
			}
			String port = fInitialAttributes.get(ProxyConnection.PORT_ATTR);
			if (port != null) {
				fPortText.setText(port);
			}
			String timeout = fInitialAttributes.get(ProxyConnection.TIMEOUT_ATTR);
			if (timeout != null) {
				fTimeoutText.setText(timeout);
			}
			String isPwd = fInitialAttributes.get(ProxyConnection.USE_PASSWORD_ATTR);
			if (isPwd != null) {
				fPasswordButton.setSelection(Boolean.parseBoolean(isPwd));
			}
			String password = fInitialAttributes.get(ProxyConnection.PASSWORD_ATTR);
			if (password != null) {
				fPasswordText.setText(password);
			}
			String passphrase = fInitialAttributes.get(ProxyConnection.PASSPHRASE_ATTR);
			if (passphrase != null) {
				fPassphraseText.setText(passphrase);
			}
			String useDefaultServer = fInitialAttributes.get(ProxyConnection.USE_DEFAULT_SERVER_ATTR);
			if (useDefaultServer != null) {
				fDefaultServerButton.setSelection(Boolean.parseBoolean(useDefaultServer));
			}
			String serverCommand = fInitialAttributes.get(ProxyConnection.SERVER_COMMAND_ATTR);
			if (serverCommand != null) {
				fServerCommandText.setText(serverCommand);
			}
		}
	}

	private void registerListeners() {
		fConnectionName.addModifyListener(fDataModifyListener);
		fHostText.addModifyListener(fDataModifyListener);
		fUserText.addModifyListener(fDataModifyListener);
		fPasswordText.addModifyListener(fDataModifyListener);
		fPassphraseText.addModifyListener(fDataModifyListener);
		fPortText.addModifyListener(fDataModifyListener);
		fTimeoutText.addModifyListener(fDataModifyListener);
		fServerCommandText.addModifyListener(fDataModifyListener);
	}

	public void setAddress(String address) {
		fInitialAttributes.put(ProxyConnection.HOSTNAME_ATTR, address);
	}

	public void setAttributes(Map<String, String> attributes) {
		fInitialAttributes.putAll(attributes);
	}

	public void setConnection(IRemoteConnectionWorkingCopy connection) {
		fConnection = connection;
	}

	public void setConnectionName(String name) {
		fInitialName = name;
	}

	public void setInvalidConnectionNames(Set<String> names) {
		fInvalidConnectionNames = names;
	}

	@Override
	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
		if (complete) {
			storeValues();
		}
	}

	public void setPort(int port) {
		fInitialAttributes.put(ProxyConnection.PORT_ATTR, Integer.toString(port));
	}

	private void setTextFieldWidthInChars(Text text, int chars) {
		text.setTextLimit(chars);
		Object data = text.getLayoutData();
		if (data instanceof GridData) {
			GC gc = new GC(text);
			FontMetrics fm = gc.getFontMetrics();
			int width = (int) (chars * fm.getAverageCharacterWidth());
			gc.dispose();
			((GridData) data).widthHint = width;
		}
	}

	public void setUsername(String username) {
		fInitialAttributes.put(ProxyConnection.USERNAME_ATTR, username);
	}

	private void storeValues() {
		if (fConnection == null) {
			try {
				fConnection = fConnectionType.newConnection(fConnectionName.getText().trim());
			} catch (RemoteConnectionException e) {
				Activator.log(e);
			}
		}
		if (fConnection != null) {
			fConnection.setName(fConnectionName.getText().trim());
			fConnection.setAttribute(ProxyConnection.HOSTNAME_ATTR, fHostText.getText().trim());
			fConnection.setAttribute(ProxyConnection.USERNAME_ATTR, fUserText.getText().trim());
			fConnection.setSecureAttribute(ProxyConnection.PASSWORD_ATTR, fPasswordText.getText().trim());
			fConnection.setSecureAttribute(ProxyConnection.PASSPHRASE_ATTR, fPassphraseText.getText().trim());
			fConnection.setAttribute(ProxyConnection.USE_PASSWORD_ATTR,
					Boolean.toString(fPasswordButton.getSelection()));
			fConnection.setAttribute(ProxyConnection.TIMEOUT_ATTR, fTimeoutText.getText().trim());
			fConnection.setAttribute(ProxyConnection.PORT_ATTR, fPortText.getText().trim());
			fConnection.setAttribute(ProxyConnection.USE_DEFAULT_SERVER_ATTR,
					Boolean.toString(fDefaultServerButton.getSelection()));
			fConnection.setAttribute(ProxyConnection.SERVER_COMMAND_ATTR, fServerCommandText.getText().trim());
		}
	}

	private void updateEnablement() {
		boolean isPasswordAuth = fPasswordButton.getSelection();
		fPasswordText.setEnabled(isPasswordAuth);
		fPassphraseText.setEnabled(!isPasswordAuth);
		fServerCommandText.setEnabled(!fDefaultServerButton.getSelection());
	}

	private String validateAdvanced() {
		try {
			Integer.parseInt(fPortText.getText().trim());
		} catch (NumberFormatException ne) {
			return Messages.ProxyNewConnectionPage_Port_is_not_valid;
		}
		try {
			Integer.parseInt(fTimeoutText.getText().trim());
		} catch (NumberFormatException ne) {
			return Messages.ProxyNewConnectionPage_Timeout_is_not_valid;
		}
		return null;
	}

	private void validateFields() {
		String message = null;
		if (fConnectionName.getText().trim().length() == 0) {
			message = Messages.ProxyNewConnectionPage_Please_enter_a_connection_name;
		} else if (isInvalidName(fConnectionName.getText().trim())) {
			message = Messages.ProxyConnectionPage_A_connection_with_that_name_already_exists;
		} else if (fHostText.getText().trim().length() == 0) {
			message = Messages.ProxyNewConnectionPage_Host_name_cannot_be_empty;
		} else if (fUserText.getText().trim().length() == 0) {
			message = Messages.ProxyNewConnectionPage_User_name_cannot_be_empty;
		} else if (!fDefaultServerButton.getSelection() && fServerCommandText.getText().trim().length() == 0) {
			message = "Server command cannot be empty";
		}
		if (message == null) {
			message = validateAdvanced();
		}

		setErrorMessage(message);
		setPageComplete(message == null);
	}

}
