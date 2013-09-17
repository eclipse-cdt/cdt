/**
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.internal.remote.jsch.ui.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.internal.remote.jsch.core.Activator;
import org.eclipse.internal.remote.jsch.core.JSchConnection;
import org.eclipse.internal.remote.jsch.core.JSchConnectionAttributes;
import org.eclipse.internal.remote.jsch.core.JSchConnectionWorkingCopy;
import org.eclipse.internal.remote.jsch.ui.messages.Messages;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.widgets.RemoteFileWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class JSchConnectionPage extends WizardPage {
	private class DataModifyListener implements ModifyListener {
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
	private Combo fCipherCombo;
	private RemoteFileWidget fFileWidget;

	private String fInitialName = "Remote Host"; //$NON-NLS-1$
	private Set<String> fInvalidConnectionNames;
	private final Map<String, String> fInitialAttributes = new HashMap<String, String>();
	private JSchConnectionWorkingCopy fConnection;

	private final IRemoteConnectionManager fConnectionManager;

	private final DataModifyListener fDataModifyListener = new DataModifyListener();

	public JSchConnectionPage(IRemoteConnectionManager connMgr) {
		super(Messages.JSchNewConnectionPage_New_Connection);
		fConnectionManager = connMgr;
		setPageComplete(false);
	}

	/**
	 * Create controls for the bottom (hideable) composite
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

			public void expansionStateChanged(ExpansionEvent e) {
				Point newSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point currentSize = parent.getSize();
				int deltaY = newSize.y - currentSize.y;
				Point shellSize = getShell().getSize();
				shellSize.y += deltaY;
				getShell().setSize(shellSize);
				getShell().layout(true, true);
			}

			public void expansionStateChanging(ExpansionEvent e) {
				// Ignore
			}
		});

		Composite advancedComp = new Composite(expComp, SWT.NONE);
		advancedComp.setLayout(new GridLayout(2, false));
		advancedComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Label portLabel = new Label(advancedComp, SWT.NONE);
		portLabel.setText(Messages.JSchNewConnectionPage_Port);
		portLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fPortText = new Text(advancedComp, SWT.BORDER | SWT.SINGLE);
		fPortText.setText(Integer.toString(JSchConnection.DEFAULT_PORT));
		fPortText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		fPortText.setTextLimit(5);

		Label timeoutLabel = new Label(advancedComp, SWT.NONE);
		timeoutLabel.setText(Messages.JSchNewConnectionPage_Timeout);
		timeoutLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fTimeoutText = new Text(advancedComp, SWT.BORDER | SWT.SINGLE);
		fTimeoutText.setText(Integer.toString(JSchConnection.DEFAULT_TIMEOUT));
		fTimeoutText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		fTimeoutText.setTextLimit(5);

		// Label cipherLabel = new Label(advancedComp, SWT.NONE);
		// cipherLabel.setText("Cipher type:");
		// cipherLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		// fCipherCombo = new Combo(advancedComp, SWT.NONE);
		// fCipherCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		expComp.setClient(advancedComp);
	}

	private void createAuthControls(Composite parent) {
		Composite controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new GridLayout(2, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label hostLabel = new Label(controls, SWT.NONE);
		hostLabel.setText(Messages.JSchNewConnectionPage_Host);
		hostLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fHostText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fHostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label userLabel = new Label(controls, SWT.NONE);
		userLabel.setText(Messages.JSchNewConnectionPage_User);
		userLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fUserText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fUserText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// User option box
		fPasswordButton = new Button(controls, SWT.RADIO);
		fPasswordButton.setText(Messages.JSchNewConnectionPage_Password_based_authentication);
		fPasswordButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		fPasswordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		// Password field
		Label passwordLabel = new Label(controls, SWT.NONE);
		passwordLabel.setText(Messages.JSchNewConnectionPage_Password);
		passwordLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fPasswordText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Key option box
		fPublicKeyButton = new Button(controls, SWT.RADIO);
		fPublicKeyButton.setText(Messages.JSchNewConnectionPage_Public_key_based_authentication);
		fPublicKeyButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		fPublicKeyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});

		// Key file selection
		fFileWidget = new RemoteFileWidget(controls, SWT.NONE, 0, null, ""); //$NON-NLS-1$
		fFileWidget.setConnection(RemoteServices.getLocalServices().getConnectionManager()
				.getConnection(IRemoteConnectionManager.LOCAL_CONNECTION_NAME));
		fFileWidget.setLabel(Messages.JSchNewConnectionPage_File_with_private_key);
		fFileWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// Passphrase field
		Label passphraseLabel = new Label(controls, SWT.NONE);
		passphraseLabel.setText(Messages.JSchNewConnectionPage_Passphrase);
		passphraseLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fPassphraseText = new Text(controls, SWT.BORDER | SWT.SINGLE);
		fPassphraseText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		fPasswordButton.setSelection(true);
		fPublicKeyButton.setSelection(false);
		updateEnablement();
	}

	public void createControl(Composite parent) {
		if (fConnection == null) {
			setDescription(Messages.JSchNewConnectionPage_New_connection_properties);
			setTitle(Messages.JSchNewConnectionPage_New_Connection);
		} else {
			setDescription(Messages.JSchConnectionPage_Edit_properties_of_an_existing_connection);
			setTitle(Messages.JSchConnectionPage_Edit_Connection);
		}
		setMessage(Messages.JSchConnectionPage_Please_enter_name_for_connection);
		setErrorMessage(null);

		GridLayout topLayout = new GridLayout(2, false);
		final Composite topControl = new Composite(parent, SWT.NONE);
		setControl(topControl);
		topControl.setLayout(topLayout);

		Label label = new Label(topControl, SWT.NONE);
		label.setText(Messages.JSchNewConnectionPage_Connection_name);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		fConnectionName = new Text(topControl, SWT.BORDER | SWT.SINGLE);
		fConnectionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fConnectionName.setEnabled(fConnection == null);

		final Group authGroup = new Group(topControl, SWT.NONE);
		authGroup.setText(Messages.JSchNewConnectionPage_Host_information);
		authGroup.setLayout(new GridLayout(1, false));
		authGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		createAuthControls(authGroup);
		createAdvancedControls(authGroup);

		loadValues();
		/*
		 * Register listeners after loading values so we don't trigger listeners
		 */
		registerListeners();
	}

	public JSchConnectionWorkingCopy getConnection() {
		return fConnection;
	}

	private boolean isInvalidName(String name) {
		if (fInvalidConnectionNames == null) {
			return fConnectionManager.getConnection(name) != null;
		}
		return fInvalidConnectionNames.contains(name);
	}

	private void loadValues() {
		if (fConnection != null) {
			fConnectionName.setText(fConnection.getName());
			fHostText.setText(fConnection.getAddress());
			fUserText.setText(fConnection.getUsername());
			fPortText.setText(Integer.toString(fConnection.getPort()));
			fTimeoutText.setText(Integer.toString(fConnection.getTimeout()));
			fPasswordButton.setSelection(fConnection.isPasswordAuth());
			if (fConnection.isPasswordAuth()) {
				fPasswordText.setText(fConnection.getPassword());
			} else {
				fPassphraseText.setText(fConnection.getPassphrase());
				fFileWidget.setLocationPath(fConnection.getKeyFile());
			}
		} else {
			fConnectionName.setText(fInitialName);
			String host = fInitialAttributes.get(JSchConnectionAttributes.ADDRESS_ATTR);
			if (host != null) {
				fHostText.setText(host);
			}
			String username = fInitialAttributes.get(JSchConnectionAttributes.USERNAME_ATTR);
			if (username != null) {
				fUserText.setText(username);
			}
			String port = fInitialAttributes.get(JSchConnectionAttributes.PORT_ATTR);
			if (port != null) {
				fPortText.setText(port);
			}
			String timeout = fInitialAttributes.get(JSchConnectionAttributes.TIMEOUT_ATTR);
			if (timeout != null) {
				fTimeoutText.setText(timeout);
			}
			String isPwd = fInitialAttributes.get(JSchConnectionAttributes.IS_PASSWORD_ATTR);
			if (isPwd != null) {
				fPasswordButton.setSelection(Boolean.parseBoolean(isPwd));
			}
			String password = fInitialAttributes.get(JSchConnectionAttributes.PASSWORD_ATTR);
			if (password != null) {
				fPasswordText.setText(password);
			}
			String passphrase = fInitialAttributes.get(JSchConnectionAttributes.PASSPHRASE_ATTR);
			if (passphrase != null) {
				fPassphraseText.setText(passphrase);
			}
			String file = fInitialAttributes.get(JSchConnectionAttributes.KEYFILE_ATTR);
			if (file != null) {
				fFileWidget.setLocationPath(file);
			}
		}
	}

	private void registerListeners() {
		fConnectionName.addModifyListener(fDataModifyListener);
		fHostText.addModifyListener(fDataModifyListener);
		fUserText.addModifyListener(fDataModifyListener);
		fPortText.addModifyListener(fDataModifyListener);
		fTimeoutText.addModifyListener(fDataModifyListener);
	}

	public void setAddress(String address) {
		fInitialAttributes.put(JSchConnectionAttributes.ADDRESS_ATTR, address);
	}

	public void setAttributes(Map<String, String> attributes) {
		fInitialAttributes.putAll(attributes);
	}

	public void setConnection(JSchConnectionWorkingCopy connection) {
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
		fInitialAttributes.put(JSchConnectionAttributes.PORT_ATTR, Integer.toString(port));
	}

	public void setUsername(String username) {
		fInitialAttributes.put(JSchConnectionAttributes.USERNAME_ATTR, username);
	}

	private void storeValues() {
		if (fConnection == null) {
			try {
				JSchConnection conn = (JSchConnection) fConnectionManager.newConnection(fConnectionName.getText().trim());
				fConnection = (JSchConnectionWorkingCopy) conn.getWorkingCopy();
			} catch (RemoteConnectionException e) {
				Activator.log(e);
			}
		}
		if (fConnection != null) {
			fConnection.setAddress(fHostText.getText().trim());
			fConnection.setUsername(fUserText.getText().trim());
			fConnection.setPassword(fPasswordText.getText().trim());
			fConnection.setPassphrase(fPassphraseText.getText().trim());
			fConnection.setKeyFile(fFileWidget.getLocationPath());
			fConnection.setIsPasswordAuth(fPasswordButton.getSelection());
			fConnection.setTimeout(Integer.parseInt(fTimeoutText.getText().trim()));
			fConnection.setPort(Integer.parseInt(fPortText.getText().trim()));
		}
	}

	private void updateEnablement() {
		boolean isPasswordAuth = fPasswordButton.getSelection();
		fPasswordText.setEnabled(isPasswordAuth);
		fPassphraseText.setEnabled(!isPasswordAuth);
		fFileWidget.setEnabled(!isPasswordAuth);
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
		if (message == null) {
			message = validatePasskey();
		}
		if (message == null) {
			message = validateAdvanced();
		}
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private String validatePasskey() {
		if (!fPasswordButton.getSelection()) {
			if (fFileWidget.getLocationPath().trim().length() == 0) {
				return Messages.JSchNewConnectionPage_Private_key_path_cannot_be_empty;
			}
			File path = new File(fFileWidget.getLocationPath().trim());
			if (!path.exists()) {
				return Messages.JSchNewConnectionPage_Private_key_file_does_not_exist;
			}
			if (!path.isFile()) {
				return Messages.JSchNewConnectionPage_Private_key_file_is_invalid;
			}
			if (!path.canRead()) {
				return Messages.JSchNewConnectionPage_Private_key_file_cannot_be_read;
			}
		}
		return null;
	}

}
