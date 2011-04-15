/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [210242] Credentials dialog should look different if password is not supported or optional
 * Richie Yu (IBM) - [241716] Handle change expired password
 * David McKnight (IBM) - [342615] when user checks "Save password" box, "Save User ID" box should automatically get checked
 ********************************************************************************/

package org.eclipse.rse.ui.dialogs;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ICredentials;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Prompt user for password.
 * This class is final due to the sensitive nature of the
 * information being prompted for.
 */
public final class SystemPasswordPromptDialog extends SystemPromptDialog implements ISystemPasswordPromptDialog {

// labels are not as big as text fields so we need to set the height for the system type 
// and hostname labels so they are equally spaced with the user ID and password entry fields
// private static final int LABEL_HEIGHT = 17;

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
	private boolean requiresPassword;
	private boolean requiresUserId;
	private ISystemValidator userIdValidator;
	private ISystemValidator passwordValidator;
	private ICredentialsValidator signonValidator;
	private IConnectorService connectorService = null;
	private boolean wasPasswordSaved = false;

	/**
	 * Constructor for SystemPasswordPromptDialog
	 * @param shell The shell in which to base this dialog.
	 * @param requiresUserId true if the userid field of the dialog must not be empty.
	 * Used only if there is no validator specified for 
	 * {@link #setUserIdValidator(ISystemValidator)}.
	 * @param requiresPassword true if the password field of the dialog must not be empty.
	 * Used only if there is no password validator specified using
	 * {@link #setPasswordValidator(ISystemValidator)}. 
	 */
	public SystemPasswordPromptDialog(Shell shell, boolean requiresUserId, boolean requiresPassword) {
		super(shell, SystemResources.RESID_PASSWORD_TITLE);
		setHelp(RSEUIPlugin.HELPPREFIX + "pwdp0000"); //$NON-NLS-1$
		this.requiresPassword = requiresPassword;
		this.requiresUserId = requiresUserId;
	}

	/**
	 * Set the connector service from which the user is attempting to do a connect action.
	 * This is used to query the system type, host name and user id to display to the user for
	 * contextual information.
	 * <p>
	 * This must be called prior to opening this dialog.
	 * @param connectorService the connector service associated with this dialog
	 */
	public void setSystemInput(IConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	/**
	 * Sets the validator for the userId. If not null it will be called per keystroke.
	 * <p>
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 * @param v a validator
	 */
	public void setUserIdValidator(ISystemValidator v) {
		userIdValidator = v;
	}

	/**
	 * Sets the validator for the password. If not null it will be called per keystroke.
	 * The default validator is null.
	 * <p>
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 * @param v a validator
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
	 * @param v a signon validator
	 */
	public void setSignonValidator(ICredentialsValidator v) {
		signonValidator = v;
	}

	/**
	 * Sets the option to force the userId and password to uppercase. This use should be rare.
	 * Use this with caution.
	 * <p>
	 * The default is false.
	 * This must be called prior to opening this dialog if something other than the default is needed.
	 * @param force true if the user id and password are to be forced to uppercase
	 */
	public void setForceToUpperCase(boolean force) {
		this.forceToUpperCase = force;
	}

	/**
	 * Call this to query the force-to-uppercase setting
	 * @return the setting for forcing the user id and password to upper case
	 */
	public boolean getForceToUpperCase() {
		return forceToUpperCase;
	}

	/**
	 * Creates the dialog controls.
	 * @param parent the containing composite control in which our controls will be created. It is assumed
	 * to have a grid layout.
	 * @return the composite control we create that nests inside the parent
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
		
		// dwd: cannot set height hints on labels since that causes cut off text for large fonts used by those with impaired vision

		// System type
		String text = SystemWidgetHelpers.appendColon(SystemResources.RESID_PASSWORD_SYSTEMTYPE_LABEL);
		Label label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		GridData gd = new GridData();
		label.setLayoutData(gd);
		label = SystemWidgetHelpers.createLabel(composite_prompts, connectorService.getHost().getSystemType().getLabel());
		gd = new GridData();
		label.setLayoutData(gd);

		// Host name
		text = SystemWidgetHelpers.appendColon(SystemResources.RESID_PASSWORD_HOSTNAME_LABEL);
		label = SystemWidgetHelpers.createLabel(composite_prompts, text);
		gd = new GridData();
		label.setLayoutData(gd);
		label = SystemWidgetHelpers.createLabel(composite_prompts, connectorService.getHostName());
		gd = new GridData();
		label.setLayoutData(gd);

		// UserId
		if (connectorService.supportsUserId()) {
			textUserId = SystemWidgetHelpers.createLabeledTextField(composite_prompts, this, SystemResources.RESID_PASSWORD_USERID_LABEL, SystemResources.RESID_PASSWORD_USERID_TIP);
		}

		// Password prompt
		if (connectorService.supportsPassword()) {
			String passwordLabel = SystemResources.RESID_PASSWORD_LABEL_OPTIONAL;
			if (connectorService.requiresPassword()) {
				passwordLabel = SystemResources.RESID_PASSWORD_LABEL;
			}
			textPassword = SystemWidgetHelpers.createLabeledTextField(composite_prompts, this, passwordLabel, SystemResources.RESID_PASSWORD_TIP);
			textPassword.setEchoChar('*');
		}

		// UserId_make_permanent checkbox
		// DY:  align user ID checkbox with entry fields
		// yantzi:5.1 move checkboxes to be below entry fields
		if (connectorService.supportsUserId()) {
			SystemWidgetHelpers.createLabel(composite_prompts, ""); //$NON-NLS-1$
			userIdPermanentCB = SystemWidgetHelpers.createCheckBox(composite_prompts, 1, this, SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_LABEL,
					SystemResources.RESID_PASSWORD_USERID_ISPERMANENT_TIP);
			userIdPermanentCB.setEnabled(false);
		}

		// Save signon information checkbox
		// DY:  align password checkbox with entry fields
		if (connectorService.supportsPassword() && !connectorService.getDenyPasswordSave()) {
			SystemWidgetHelpers.createLabel(composite_prompts, ""); //$NON-NLS-1$
			savePasswordCB = SystemWidgetHelpers.createCheckBox(composite_prompts, 1, this, SystemResources.RESID_PASSWORD_SAVE_LABEL, SystemResources.RESID_PASSWORD_SAVE_TOOLTIP);
			savePasswordCB.setSelection(savePassword);
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
			userId = RSEPreferencesManager.getUserId(connectorService.getHost().getSystemType().getId());
		}
		if (textUserId != null && userId != null) {
			textUserId.setText(userId);
			textUserId.setSelection(0, userId.length());
		}
		if (textPassword != null && password != null) {
			textPassword.setText(password);
			textPassword.setSelection(0, password.length());
		}
		
		String defaultUserId = connectorService.getHost().getDefaultUserId();
		wasPasswordSaved = defaultUserId.equals(userId);
		
		if (wasPasswordSaved && userIdPermanentCB != null){
			userIdPermanentCB.setSelection(true);
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
		clearErrorMessage();
		SystemMessage m = checkUserId();
		if (m == null) {
			m = checkPassword();
		}
		if (m != null) {
			setErrorMessage(m);
		}
		okButton.setEnabled(m == null);
		if (userId == null || originalUserId == null) {
			userIdChanged = (userId != originalUserId);
		} else {
			userIdChanged = !userId.equals(originalUserId);
		}
		if (userIdPermanentCB != null) {		
			userIdPermanentCB.setEnabled(userIdChanged || !wasPasswordSaved);
		}
	}
	
	/**
	 * Performs the actual validation check for the user id.
	 * Delegates the request to an <code>ISystemValidator</code> object.
	 * @return the message returned by the validator or null.
	 */
	private SystemMessage checkUserId() {
		internalGetUserId();
		SystemMessage m = null;
		if (connectorService.supportsUserId() && validate) {
			if (userIdValidator != null) {
				m = userIdValidator.validate(userId);
			} else if (requiresUserId && userId.length() == 0) {
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
		clearErrorMessage();
		SystemMessage m = checkPassword();
		if (m == null) {
			m = checkUserId();
		}
		if (m != null) {
			setErrorMessage(m);
		}
		okButton.setEnabled(m == null);
		if (savePasswordCB != null) {
			savePasswordCB.setEnabled(!(requiresPassword && password.length() == 0));
		}
	}
	
	/**
	 * Checks the value of the password instance variable.
	 */
	private SystemMessage checkPassword() {
		internalGetPassword();
		SystemMessage m = null;
		if (connectorService.supportsPassword() && validate) {
			if (passwordValidator != null) {
				m = passwordValidator.validate(password);
			} else if (requiresPassword && password.length() == 0) {
				m = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PASSWORD_EMPTY);
			}
		}
		return m;
	}

	/**
	 * @return the userId entered by user
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return the password may have been modified by the user.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password, may be null if no password is available.
	 * @param password the password to provide for the password field.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return true if the user changed the user id
	 */
	public boolean getIsUserIdChanged() {
		return userIdChanged;
	}

	/**
	 * @return true if the user elected to make the changed user Id a permanent change.
	 */
	public boolean getIsUserIdChangePermanent() {
		return userIdPermanent;
	}

	/**
	 * @return true if the user elected to make the changed user Id a permanent change.
	 */
	public boolean getIsSavePassword() {
		return savePassword;
	}

	/**
	 * Preselect the save password checkbox.  Default value is to not 
	 * select the save password checkbox.
	 * @param save true if the save password box should be checked.
	 */
	public void setSavePassword(boolean save) {
		savePassword = save;
	}

	/**
	 * Verifies all input. Sets the error message if there are any conditions that are found.
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
		if (controlInError != null) {
			controlInError.setFocus(); // validate methods already displayed error message
		}
	}

	/**
	 * Called when user presses OK button. 
	 * @return true to close dialog, false to not close dialog.
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
			String hostName = connectorService.getHostName();
			IRSESystemType systemType = connectorService.getHost().getSystemType();
			ICredentials credentials = new SystemSignonInformation(hostName, userId, password, systemType);
			SystemMessage m = signonValidator.validate(credentials);
			// update the password in case an expired password was changed in validate - ry
			password = credentials.getPassword();  
			setErrorMessage(m);
		}
		boolean closeDialog = (getErrorMessage() == null); 
		return closeDialog;
	}
	
	// override of super method
	public void handleEvent(Event e){
		if (e.widget == savePasswordCB){
			if (savePasswordCB.getSelection() && userIdPermanentCB != null){
				// make sure the user is saved too - otherwise uid/password might not be retrieved on restart
				userIdPermanentCB.setSelection(true);
			}					
		}
		super.handleEvent(e);
	}
}