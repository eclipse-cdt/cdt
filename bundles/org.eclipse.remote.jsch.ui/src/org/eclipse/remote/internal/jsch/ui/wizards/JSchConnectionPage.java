/**
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *     Patrick Tasse - [461541] fix handling of default attributes
 */
package org.eclipse.remote.internal.jsch.ui.wizards;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemotePortForwardingService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.Activator;
import org.eclipse.remote.internal.jsch.core.JSchConnection;
import org.eclipse.remote.internal.jsch.ui.messages.Messages;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
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

public class JSchConnectionPage extends WizardPage {

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
	private Text fHostText;
	private Text fUserText;
	private Text fPasswordText;
	private Text fPassphraseText;
	private Text fPortText;
	private Text fTimeoutText;

	private String fInitialName = "Remote Host"; //$NON-NLS-1$
	private Set<String> fInvalidConnectionNames;
	private final Map<String, String> fInitialAttributes = new HashMap<String, String>();
	private IRemoteConnectionWorkingCopy fConnection;

	private final IRemoteConnectionType fConnectionType;

	private final DataModifyListener fDataModifyListener = new DataModifyListener();
	private RemoteConnectionWidget fProxyConnectionWidget;
	private Text fProxyCommandText;
	private static final String PREFS_PAGE_ID_NET_PROXY = "org.eclipse.ui.net.NetPreferences"; //$NON-NLS-1$

	public JSchConnectionPage(IRemoteConnectionType connectionType) {
		super(Messages.JSchNewConnectionPage_New_Connection);
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
		expComp.setText(Messages.JSchNewConnectionPage_Advanced);
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
		settingsComp.setText(Messages.JSchConnectionPage_Settings0);
		settingsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		settingsComp.setLayout(new GridLayout(2, false));

		Label portLabel = new Label(settingsComp, SWT.NONE);
		portLabel.setText(Messages.JSchNewConnectionPage_Port);
		fPortText = new Text(settingsComp, SWT.BORDER | SWT.SINGLE);
		fPortText.setText(Integer.toString(JSchConnection.DEFAULT_PORT));
		fPortText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		setTextFieldWidthInChars(fPortText, 5);

		Label timeoutLabel = new Label(settingsComp, SWT.NONE);
		timeoutLabel.setText(Messages.JSchNewConnectionPage_Timeout);
		fTimeoutText = new Text(settingsComp, SWT.BORDER | SWT.SINGLE);
		fTimeoutText.setText(Integer.toString(JSchConnection.DEFAULT_TIMEOUT));
		fTimeoutText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		setTextFieldWidthInChars(fTimeoutText, 5);

		Group proxyComp = new Group(advancedComp, SWT.NONE);
		proxyComp.setText(Messages.JSchConnectionPage_Proxy);
		proxyComp.setLayout(new GridLayout(1, false));
		proxyComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		createProxyControls(proxyComp);

		expComp.setClient(advancedComp);
	}

	private void createAuthControls(Composite parent) {
		Composite controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new GridLayout(3, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label hostLabel = new Label(controls, SWT.NONE);
		hostLabel.setText(Messages.JSchNewConnectionPage_Host);
		fHostText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label userLabel = new Label(controls, SWT.NONE);
		userLabel.setText(Messages.JSchNewConnectionPage_User);
		fUserText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fUserText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// Key option box
		fPublicKeyButton = new Button(controls, SWT.RADIO);
		fPublicKeyButton.setText(Messages.JSchNewConnectionPage_Public_key_based_authentication);
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
		link.setText(Messages.JSchConnectionPage_KeysAtSSH2);

		// Passphrase field
		Label passphraseLabel = new Label(controls, SWT.NONE);
		passphraseLabel.setText(Messages.JSchNewConnectionPage_Passphrase);
		fPassphraseText = new Text(controls, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		fPassphraseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// User option box
		fPasswordButton = new Button(controls, SWT.RADIO);
		fPasswordButton.setText(Messages.JSchNewConnectionPage_Password_based_authentication);
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
		passwordLabel.setText(Messages.JSchNewConnectionPage_Password);
		fPasswordText = new Text(controls, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
		fPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		fPasswordButton.setSelection(JSchConnection.DEFAULT_IS_PASSWORD);
		fPublicKeyButton.setSelection(!JSchConnection.DEFAULT_IS_PASSWORD);
		controls.setTabList(new Control[] { fHostText, fUserText, fPublicKeyButton, fPassphraseText, fPasswordButton, fPasswordText });
	}

	@Override
	public void createControl(Composite parent) {
		if (fConnection == null) {
			setDescription(Messages.JSchNewConnectionPage_New_connection_properties);
			setTitle(Messages.JSchNewConnectionPage_New_Connection);
			setMessage(Messages.JSchConnectionPage_Initial_Message);
		} else {
			setDescription(Messages.JSchConnectionPage_Edit_properties_of_an_existing_connection);
			setTitle(Messages.JSchConnectionPage_Edit_Connection);
		}
		setErrorMessage(null);

		GridLayout topLayout = new GridLayout(2, false);
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);

		Label label = new Label(topControl, SWT.NONE);
		label.setText(Messages.JSchNewConnectionPage_Connection_name);

		fConnectionName = new Text(topControl, SWT.BORDER | SWT.SINGLE);
		fConnectionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fConnectionName.setEnabled(fConnection == null);

		final Group authGroup = new Group(topControl, SWT.NONE);
		authGroup.setText(Messages.JSchNewConnectionPage_Host_information);
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

	/**
	 * Create controls for the bottom (hideable) proxy composite
	 *
	 * @param mold
	 *
	 */
	private void createProxyControls(final Composite proxyComp) {
		Label lblConnection = new Label(proxyComp, SWT.WRAP);
		lblConnection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblConnection.setText(Messages.JSchConnectionPage_SelectConnection);

		fProxyConnectionWidget = new RemoteConnectionWidget(proxyComp, SWT.NONE, null, 0);
		fProxyConnectionWidget.filterConnections(IRemoteConnectionHostService.class, IRemotePortForwardingService.class);

		Label lblCommand = new Label(proxyComp, SWT.WRAP);
		lblCommand.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblCommand.setText(Messages.JSchConnectionPage_SelectCommand);

		fProxyCommandText = new Text(proxyComp, SWT.BORDER);
		fProxyCommandText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Link link = new Link(proxyComp, SWT.WRAP);
		final GridData linkLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		link.setLayoutData(linkLayoutData);
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(getShell(), PREFS_PAGE_ID_NET_PROXY,
						new String[] { PREFS_PAGE_ID_NET_PROXY }, null);
				dlg.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});

		linkLayoutData.widthHint = 400;
		link.setText(Messages.JSchConnectionPage_Help);
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
			fHostText.setText(fConnection.getAttribute(JSchConnection.ADDRESS_ATTR));
			fUserText.setText(fConnection.getAttribute(JSchConnection.USERNAME_ATTR));
			String portStr = fConnection.getAttribute(JSchConnection.PORT_ATTR);
			fPortText.setText(portStr.isEmpty() ? Integer.toString(JSchConnection.DEFAULT_PORT) : portStr);
			String timeoutStr = fConnection.getAttribute(JSchConnection.TIMEOUT_ATTR);
			fTimeoutText.setText(timeoutStr.isEmpty() ? Integer.toString(JSchConnection.DEFAULT_TIMEOUT) : timeoutStr);
			String isPwdStr = fConnection.getAttribute(JSchConnection.IS_PASSWORD_ATTR);
			boolean isPwd = isPwdStr.isEmpty() ? JSchConnection.DEFAULT_IS_PASSWORD : Boolean.parseBoolean(isPwdStr);
			fPasswordButton.setSelection(isPwd);
			fPublicKeyButton.setSelection(!isPwd);
			fPasswordText.setText(fConnection.getSecureAttribute(JSchConnection.PASSWORD_ATTR));
			fPassphraseText.setText(fConnection.getSecureAttribute(JSchConnection.PASSPHRASE_ATTR));
			fProxyCommandText.setText(fConnection.getAttribute(JSchConnection.PROXYCOMMAND_ATTR));
			JSchConnection proxyConn = fConnection.getService(JSchConnection.class).getProxyConnection();
			if (proxyConn == null) {
				// Use local connection
				fProxyConnectionWidget.setConnection(manager.getLocalConnectionType().getConnections().get(0));
			} else {
				fProxyConnectionWidget.setConnection(proxyConn.getRemoteConnection());
			}
		} else {
			fConnectionName.setText(fInitialName);
			String host = fInitialAttributes.get(JSchConnection.ADDRESS_ATTR);
			if (host != null) {
				fHostText.setText(host);
			}
			String username = fInitialAttributes.get(JSchConnection.USERNAME_ATTR);
			if (username != null) {
				fUserText.setText(username);
			}
			String port = fInitialAttributes.get(JSchConnection.PORT_ATTR);
			if (port != null) {
				fPortText.setText(port);
			}
			String timeout = fInitialAttributes.get(JSchConnection.TIMEOUT_ATTR);
			if (timeout != null) {
				fTimeoutText.setText(timeout);
			}
			String isPwd = fInitialAttributes.get(JSchConnection.IS_PASSWORD_ATTR);
			if (isPwd != null) {
				fPasswordButton.setSelection(Boolean.parseBoolean(isPwd));
			}
			String password = fInitialAttributes.get(JSchConnection.PASSWORD_ATTR);
			if (password != null) {
				fPasswordText.setText(password);
			}
			String passphrase = fInitialAttributes.get(JSchConnection.PASSPHRASE_ATTR);
			if (passphrase != null) {
				fPassphraseText.setText(passphrase);
			}
			fProxyConnectionWidget.setConnection(manager.getLocalConnectionType().getConnections().get(0));
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
		fProxyCommandText.addModifyListener(fDataModifyListener);
		fProxyConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateFields();
				getContainer().updateButtons();
			}
		});
	}

	public void setAddress(String address) {
		fInitialAttributes.put(JSchConnection.ADDRESS_ATTR, address);
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
		fInitialAttributes.put(JSchConnection.PORT_ATTR, Integer.toString(port));
	}

	private void setTextFieldWidthInChars(Text text, int chars) {
		text.setTextLimit(chars);
		Object data = text.getLayoutData();
		if (data instanceof GridData) {
			GC gc = new GC(text);
			FontMetrics fm = gc.getFontMetrics();
			int width = chars * fm.getAverageCharWidth();
			gc.dispose();
			((GridData) data).widthHint = width;
		}
	}

	public void setUsername(String username) {
		fInitialAttributes.put(JSchConnection.USERNAME_ATTR, username);
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
			fConnection.setAttribute(JSchConnection.ADDRESS_ATTR, fHostText.getText().trim());
			fConnection.setAttribute(JSchConnection.USERNAME_ATTR, fUserText.getText().trim());
			fConnection.setSecureAttribute(JSchConnection.PASSWORD_ATTR, fPasswordText.getText().trim());
			fConnection.setSecureAttribute(JSchConnection.PASSPHRASE_ATTR, fPassphraseText.getText().trim());
			fConnection.setAttribute(JSchConnection.IS_PASSWORD_ATTR, Boolean.toString(fPasswordButton.getSelection()));
			fConnection.setAttribute(JSchConnection.TIMEOUT_ATTR, fTimeoutText.getText().trim());
			fConnection.setAttribute(JSchConnection.PORT_ATTR, fPortText.getText().trim());
			fConnection.setAttribute(JSchConnection.PROXYCOMMAND_ATTR, fProxyCommandText.getText().trim());
			IRemoteConnection proxyConnection = fProxyConnectionWidget.getConnection();
			IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
			String proxyConnectionName = ""; //$NON-NLS-1$
			if (proxyConnection != null && proxyConnection.getConnectionType() != manager.getLocalConnectionType()) {
				proxyConnectionName = proxyConnection.getName();
			}
			fConnection.setAttribute(JSchConnection.PROXYCONNECTION_ATTR, proxyConnectionName);
		}
	}

	private void updateEnablement() {
		boolean isPasswordAuth = fPasswordButton.getSelection();
		fPasswordText.setEnabled(isPasswordAuth);
		fPassphraseText.setEnabled(!isPasswordAuth);
	}

	private String validateAdvanced() {
		try {
			Integer.parseInt(fPortText.getText().trim());
		} catch (NumberFormatException ne) {
			return Messages.JSchNewConnectionPage_Port_is_not_valid;
		}
		try {
			Integer.parseInt(fTimeoutText.getText().trim());
		} catch (NumberFormatException ne) {
			return Messages.JSchNewConnectionPage_Timeout_is_not_valid;
		}
		// if (fCipherCombo.getSelectionIndex() == -1) {
		// return "Invalid cipher type";
		// }
		return null;
	}

	private void validateFields() {
		String message = null;
		if (fConnectionName.getText().trim().length() == 0) {
			message = Messages.JSchNewConnectionPage_Please_enter_a_connection_name;
		} else if (isInvalidName(fConnectionName.getText().trim())) {
			message = Messages.JSchConnectionPage_A_connection_with_that_name_already_exists;
		} else if (fHostText.getText().trim().length() == 0) {
			message = Messages.JSchNewConnectionPage_Host_name_cannot_be_empty;
		} else if (fUserText.getText().trim().length() == 0) {
			message = Messages.JSchNewConnectionPage_User_name_cannot_be_empty;
		}
		if (message == null && fProxyConnectionWidget.getConnection() == null) {
			message = Messages.JSchConnectionPage_selectProxyConnection;
		}
		if (message == null) {
			message = validateAdvanced();
		}

		setErrorMessage(message);
		setPageComplete(message == null);
	}

}
