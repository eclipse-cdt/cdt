/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - copied from org.eclipse.team.cvs.ui
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog for prompting for a username and password
 */
public class UserValidationDialog extends TrayDialog {
	// widgets
	protected Text usernameField;
	protected Text passwordField;
	protected Button allowCachingButton;

	protected String domain;
	protected String defaultUsername;
	protected String password = null;
	protected boolean allowCaching = false;
	protected Image keyLockImage;

	// whether or not the username can be changed
	protected boolean isUsernameMutable = true;
	protected String username = null;
	protected String message = null;

	/**
	 * Creates a new UserValidationDialog.
	 *
	 * @param parentShell  the parent shell
	 * @param location  the location
	 * @param defaultName  the default user name
	 * @param message  a mesage to display to the user
	 */
	public UserValidationDialog(Shell parentShell, String location, String defaultName, String message) {
		super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
		this.defaultUsername = defaultName;
		this.domain = location;
		this.message = message;
	}
	/**
	 * @see Window#configureShell
	 */
	@Override
    protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(SshMessages.UserValidationDialog_required);
		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, ISshConstants.HELP_USER_VALIDATION_DIALOG);
	}
	/**
	 * @see Window#create
	 */
	@Override
    public void create() {
		super.create();
		// add some default values
		usernameField.setText(defaultUsername);

		if (isUsernameMutable) {
			// give focus to username field
			usernameField.selectAll();
			usernameField.setFocus();
		} else {
			usernameField.setEditable(false);
			passwordField.setFocus();
		}
	}

	/**
	 * @see Dialog#createDialogArea
	 */
	@Override
    protected Control createDialogArea(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite imageComposite = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		imageComposite.setLayout(layout);
		imageComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Composite main = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label imageLabel = new Label(imageComposite, SWT.NONE);
		//keyLockImage = TeamImages.getImageDescriptor(ITeamUIImages.IMG_KEY_LOCK).createImage();
		//imageLabel.setImage(keyLockImage);
		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		imageLabel.setLayoutData(data);

		if (message != null) {
			Label messageLabel = new Label(main, SWT.WRAP);
			messageLabel.setText(message);
			data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 3;
			data.widthHint = 300;
			messageLabel.setLayoutData(data);
		}
		if (domain != null) {
			Label d = new Label(main, SWT.WRAP);
			d.setText(SshMessages.UserValidationDialog_5);
			data = new GridData();
			d.setLayoutData(data);
			Label label = new Label(main, SWT.WRAP);
			if (isUsernameMutable) {
				label.setText(NLS.bind(SshMessages.UserValidationDialog_labelUser, new String[] { domain }));
			} else {
				label.setText(NLS.bind(SshMessages.UserValidationDialog_labelPassword, (new Object[]{defaultUsername, domain})));
			}
			data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 2;
			data.widthHint = 300;
			label.setLayoutData(data);
		}
		createUsernameFields(main);
		createPasswordFields(main);

		if(domain != null) {
			allowCachingButton = new Button(main, SWT.CHECK);
			allowCachingButton.setText(SshMessages.UserValidationDialog_6);
			data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			data.horizontalSpan = 3;
			allowCachingButton.setLayoutData(data);
			allowCachingButton.addSelectionListener(new SelectionAdapter() {
				@Override
                public void widgetSelected(SelectionEvent e) {
					allowCaching = allowCachingButton.getSelection();
				}
			});
			Composite warningComposite = new Composite(main, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginHeight = 0;
			warningComposite.setLayout(layout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 3;
			warningComposite.setLayoutData(data);
			Label warningLabel = new Label(warningComposite, SWT.NONE);
			warningLabel.setImage(getImage(DLG_IMG_MESSAGE_WARNING));
			warningLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
			Label warningText = new Label(warningComposite, SWT.WRAP);
			warningText.setText(SshMessages.UserValidationDialog_7);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 300;
			warningText.setLayoutData(data);
		}

        Dialog.applyDialogFont(parent);

		return main;
	}

	/**
	 * Creates the three widgets that represent the password entry area.
	 *
	 * @param parent  the parent of the widgets
	 */
	protected void createPasswordFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(SshMessages.UserValidationDialog_password);

		passwordField = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		passwordField.setLayoutData(data);
	}
	/**
	 * Creates the three widgets that represent the user name entry area.
	 *
	 * @param parent  the parent of the widgets
	 */
	protected void createUsernameFields(Composite parent) {
		new Label(parent, SWT.NONE).setText(SshMessages.UserValidationDialog_user);

		usernameField = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		usernameField.setLayoutData(data);
	}

	/**
	 * Returns the password entered by the user, or null
	 * if the user cancelled.
	 *
	 * @return the entered password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the username entered by the user, or null
	 * if the user cancelled.
	 *
	 * @return the entered username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns <code>true</code> if the save password checkbox was selected.
	 * @return <code>true</code> if the save password checkbox was selected and <code>false</code>
	 * otherwise.
	 */
	public boolean getAllowCaching() {
		return allowCaching;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The default implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	@Override
    protected void okPressed() {
		password = passwordField.getText();
		username = usernameField.getText();

		super.okPressed();
	}
	/**
	 * Sets whether or not the username field should be mutable.
	 * This method must be called before create(), otherwise it
	 * will be ignored.
	 *
	 * @param value  whether the username is mutable
	 */
	public void setUsernameMutable(boolean value) {
		isUsernameMutable = value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
    public boolean close() {
		if(keyLockImage != null) {
			keyLockImage.dispose();
		}
		return super.close();
	}
}
