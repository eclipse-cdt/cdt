/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;

import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Prompt user for password.
 * This class is final due to the sensitive nature of the information being prompted for.
 */
public final class SystemPasswordPromptDialog extends SystemPromptDialog implements ISystemPasswordPromptDialog {

	// labels are not as big as text fields so we need to set the height for the system type 
	// and hostname labels so they are equally spaced with the user ID and password entry fields
//	private static final int LABEL_HEIGHT = 17;

	private Text textPassword;
	private Text textUserId;
	private Button userIdPermanentCB;
	private Button savePasswordCB;
	private String originalUserId;
	private String userId;
	private String password;
	private boolean userIdPermanent = false;
	private boolean savePassword = false;
	private boolean forceToUpperCase;
	private boolean userIdChanged = false;
	private boolean validate = true;
	private ISystemValidator userIdValidator;
	private ISystemValidator passwordValidator;
	private ISignonValidator signonValidator;
	private IConnectorService connectorService = null;

	/**
	 * Constructor for SystemPasswordPromptDialog
	 */
	public SystemPasswordPromptDialog(Shell shell) {
		super(shell, SystemResources.RESID_PASSWORD_TITLE);
		setHelp(RSEUIPlugin.HELPPREFIX + "pwdp0000");
	}

	/**
	 * Set the connector service from which the user is attempting to do a connect action.
	 * This is used to query the system type, host name and user id to display to the user for
	 * contextual information.
	 * <p>
	 * This must be called prior to opening this dialog.
	 */
	public void setSystemInput(IConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	/**
	 * Sets the validator for the userId. If not null it will be called per keystroke.
	 * <p>
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 */
	public void setUserIdValidator(ISystemValidator v) {
		userIdValidator = v;
	}

	/**
	 * Sets the validator for the password. If not null it will be called per keystroke.
	 * The default validator is null.
	 * <p>
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 */
	public void setPasswordValidator(ISystemValidator v) {
		passwordValidator = v;
	}

	/**
	 * Sets the validator for the signon. 
	 * The default validator is null.
	 * If not null the validator will be called when the user presses OK.
	 * <p>
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 */
	public void setSignonValidator(ISignonValidator v) {
		signonValidator = v;
	}

	/**
	 * Sets the option to force the userId and password to uppercase.
	 * <p>
	 * The default is false.
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 */
	public void setForceToUpperCase(boolean force) {
		this.forceToUpperCase = force;
	}

	/**
	 * Call this to query the force-to-uppercase setting
	 */
	public boolean getForceToUpperCase() {
		return forceToUpperCase;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		
		// top level composite  	    
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(composite, 2);

		// yantzi: artemis 6.0, at request of zOS team I am changing the system type and hostname 
		// to labels so they are clearer to read than non-editable entry fields    

		// System type
		String text = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_SYSTEMTYPE_READONLY_LABEL);
		Label label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		GridData gd = new GridData();
//		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);
		label = SystemWidgetHelpers.createLabel(composite_prompts, connectorService.getHostType());
		gd = new GridData();
//		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);

		// Host name
		text = SystemWidgetHelpers.appendColon(SystemResources.RESID_CONNECTION_HOSTNAME_READONLY_LABEL);
		label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		gd = new GridData();
//		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);
		label = SystemWidgetHelpers.createLabel(composite_prompts, connectorService.getHostName());
		gd = new GridData();
//		gd.heightHint = LABEL_HEIGHT;
		label.setLayoutData(gd);

		// UserId
		if (connectorService.supportsUserId()) {
			textUserId = SystemWidgetHelpers.createLabeledTextField(composite_prompts, this, SystemResources.RESID_CONNECTION_USERID_LABEL, SystemResources.RESID_CONNECTION_USERID_TIP);
		}

		// Password prompt
		if (connectorService.supportsPassword()) {
			textPassword = SystemWidgetHelpers.createLabeledTextField(composite_prompts, this, SystemResources.RESID_PASSWORD_LABEL, SystemResources.RESID_PASSWORD_TIP);
			textPassword.setEchoChar('*');
		}

		// UserId_make_permanent checkbox
		// DY:  align user ID checkbox with entry fields
		// yantzi:5.1 move checkboxes to be below entry fields
		if (connectorService.supportsUserId()) {
			SystemWidgetHelpers.createLabel(composite_prompts, "");
			userIdPermanentCB = SystemWidgetHelpers.createCheckBox(composite_prompts, 1, this, SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_LABEL,
					SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_TIP);
			userIdPermanentCB.setEnabled(false);
		}

		// Save signon information checkbox
		// DY:  align password checkbox with entry fields
		if (connectorService.supportsPassword()) {
			SystemWidgetHelpers.createLabel(composite_prompts, "");
			savePasswordCB = SystemWidgetHelpers.createCheckBox(composite_prompts, 1, this, SystemResources.RESID_PASSWORD_SAVE_LABEL, SystemResources.RESID_PASSWORD_SAVE_TOOLTIP);
			savePasswordCB.setSelection(savePassword);
			// disable until the user enters something for consistency with the save user ID checkbox
			savePasswordCB.setEnabled(false);
		}

		initializeInput();

		// add keystroke listeners...
		if (textUserId != null) {
			textUserId.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					processUserIdField();
				}
			});
		}
		if (textPassword != null) {
			textPassword.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					processPasswordField();
				}
			});
		}

		return composite;
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		okButton.setEnabled(true);
		processUserIdField();
		processPasswordField();
		if (textUserId != null) {
			if (userId.length() == 0 || textPassword == null) {
				return textUserId;
			}
		}
		if (textPassword != null) {
			textPassword.selectAll();
			return textPassword;
		}
		if (okButton.isEnabled()) return okButton;
		return cancelButton;
	}

	/**
	 * Initialize values using input data
	 */
	private void initializeInput() {
		originalUserId = connectorService.getUserId();
		userId = originalUserId;
		if (connectorService.supportsUserId() && (userId == null || userId.length() == 0)) {
			userId = SystemPreferencesManager.getPreferencesManager().getDefaultUserId(connectorService.getHostType());
		}
		if (textUserId != null && userId != null) {
			textUserId.setText(userId);
		}
		if (textPassword != null && password != null) {
			textPassword.setText(password);
		}
	}

	/**
	 * Retrieves the userId entered by user
	 */
	private void internalGetUserId() {
		if (textUserId != null) {
			userId = textUserId.getText().trim();
		}
	}

	/**
	 * Retrieves the password entered by user
	 */
	private void internalGetPassword() {
		if (textPassword != null) {
			password = textPassword.getText().trim();
		}
	}

	/**
	 * Retrieves the value of the "save user id" checkbox
	 */
	private void internalGetIsUserIdChangePermanent() {
		if (userIdPermanentCB != null) {
			userIdPermanent = userIdPermanentCB.isEnabled() && userIdPermanentCB.getSelection();
		}
	}

	/**
	 * Retrieves the value of the "save password" checkbox
	 */
	private void internalGetIsSavePassword() {
		if (savePasswordCB != null) {
			savePassword = savePasswordCB.isEnabled() && savePasswordCB.getSelection();
		}
	}

	/**
	 * This method is called whenever the text changes in the user id input field.
	 * Checks the user id field and if there are no errors, other fields on the dialog.
	 * If an error was reported it is displayed on the message line.
	 * @see #setUserIdValidator(ISystemValidator)	
	 */
	private void processUserIdField() {
		internalGetUserId();
		internalGetPassword();
		SystemMessage m = checkUserId();
		if (m == null) {
			m = checkPassword();
		}
		okButton.setEnabled(m == null);
		setErrorMessage(m);
		if (userId == null || originalUserId == null) {
			userIdChanged = (userId != originalUserId);
		} else {
			userIdChanged = !userId.equals(originalUserId);
		}
		if (userIdPermanentCB != null) {
			userIdPermanentCB.setEnabled(userIdChanged);
		}
	}
	
	/**
	 * Performs the actual validation check for the user id.
	 * Delegates the request to an <code>ISystemValidator</code> object.
	 * @return the message returned by the validator or null.
	 */
	private SystemMessage checkUserId() {
		SystemMessage m = null;
		if (connectorService.supportsUserId() && validate) {
			if (userIdValidator != null) {
				m = userIdValidator.validate(userId);
			} else if (connectorService.requiresUserId() && userId.length() == 0) {
				m = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_USERID_EMPTY);
			}
		}
		return m;
	}

	/**
	 * This method is called whenever the text changes in the password input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setPasswordValidator(ISystemValidator)	
	 */
	private void processPasswordField() {
		internalGetUserId();
		internalGetPassword();
		SystemMessage m = checkPassword();
		if (m == null) {
			m = checkUserId();
		}
		okButton.setEnabled(m == null);
		setErrorMessage(m);
		if (savePasswordCB != null) {
			savePasswordCB.setEnabled(password.length() > 0); // yantzi: artemis 6.0, disable save checkbox when blank
		}
	}
	
	/**
	 * Checks the value of the password instance variable.
	 */
	private SystemMessage checkPassword() {
		SystemMessage m = null;
		if (connectorService.supportsPassword() && validate) {
			if (passwordValidator != null) {
				m = passwordValidator.validate(password);
			} else if (connectorService.requiresPassword() && password.length() == 0) {
				m = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_EMPTY);
			}
		}
		return m;
	}

	/**
	 * Return the userId entered by user
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the password may have been modified by the user.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password, may be null if no password is available.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return true if the user changed the user id
	 */
	public boolean getIsUserIdChanged() {
		return userIdChanged;
	}

	/**
	 * Return true if the user elected to make the changed user Id a permanent change.
	 */
	public boolean getIsUserIdChangePermanent() {
		return userIdPermanent;
	}

	/**
	 * Return true if the user elected to make the changed user Id a permanent change.
	 */
	public boolean getIsSavePassword() {
		return savePassword;
	}

	/**
	 * Preselect the save password checkbox.  Default value is to not 
	 * select the save password checkbox.
	 */
	public void setSavePassword(boolean save) {
		savePassword = save;
	}

	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	private void verify() {
		Control controlInError = null;
		processUserIdField();
		if (getErrorMessage() != null) {
			controlInError = textUserId;
		} else {
			processPasswordField();
			if (getErrorMessage() != null) {
				controlInError = textPassword;
			}
		}
		if (getErrorMessage() != null) {
			controlInError.setFocus(); // validate methods already displayed error message
		}
	}

	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() {
		setBusyCursor(true);
		internalGetPassword();
		internalGetUserId();
		internalGetIsUserIdChangePermanent();
		internalGetIsSavePassword();
		if (forceToUpperCase) {
			userId = userId.toUpperCase();
			password = password.toUpperCase();
			validate = false;
			textUserId.setText(userId);
			textPassword.setText(password);
			validate = true;
		}

		verify();
		setBusyCursor(false); // phil        

		// If all inputs are OK then validate signon
		if (getErrorMessage() == null && (signonValidator != null)) {
			SystemMessage m = signonValidator.isValid(this, userId, password);
			setErrorMessage(m);
		}
		boolean closeDialog = (getErrorMessage() == null); 
		return closeDialog;
	}
}