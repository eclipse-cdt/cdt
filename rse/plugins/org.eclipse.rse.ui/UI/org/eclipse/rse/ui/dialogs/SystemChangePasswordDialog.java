/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
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
 * A SystemChangePasswordDialog is typically presented when the password on the remote system has expired 
 * and a new one needs to be saved. This dialog presents presents two enabled text fields - the first holds 
 * the new password, the second holds its confirmation. 
 * There is also a checkbox to save the password. Actually saving the password is the responsibility of the client.
 */
public class SystemChangePasswordDialog extends SystemPromptDialog 
{
	private String _hostname;
	private String _user;
	private String _newPassword = "";
	
	private Text _txtNewPassword;
	private Text _txtConfirmPassword;
	
    private boolean savePassword = false;
    protected Button _chkBoxSavePassword;
    
    private boolean newPasswordModified = false;
    private boolean confirmModified = false;
	
	/**
	 * Construct a new SystemChangePasswordDialog. Since this dialog is asking for a new password
	 * there is no need to supply the old password, however a remote system will usually require
	 * the old password to effect a change. Thus clients of this class would typically be expected
	 * to have this available.
	 * @param shell The shell the dialog will use to present itself.
	 * @param hostname The remote host name.
	 * @param userid The user id that will be presented. May be the empty string.
	 * @param msg The message that will be presented when the dialog is initially shown. This may be null.
	 */
	public SystemChangePasswordDialog(Shell shell, String hostname, String userid, SystemMessage msg) 
	{
		super(shell, SystemResources.RESID_CHANGE_PASSWORD_TITLE, false);
		_hostname = hostname;
		_user = userid;
		setErrorMessage(msg);
	}

	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return _txtNewPassword;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {

		Label label = null;		
		Composite c = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		c.setLayout(layout);
		
		// Host Name
		label = new Label(c, SWT.NONE);
		label.setText(SystemResources.RESID_PREF_SIGNON_HOSTNAME_LABEL);
	
		Text system = SystemWidgetHelpers.createTextField(c, null);
		system.setText(_hostname);
		system.setEditable(false);
		system.setEnabled(false);
		system.setToolTipText(SystemResources.RESID_PREF_SIGNON_HOSTNAME_TOOLTIP);
		((GridData) system.getLayoutData()).widthHint = 75;
		
		// User ID
		label = new Label(c, SWT.NONE);
		label.setText(SystemResources.RESID_PREF_SIGNON_USERID_LABEL);
		
		Text userID = SystemWidgetHelpers.createTextField(c, null);
		userID.setText(_user);
		userID.setEditable(false);
		userID.setEnabled(false);
		userID.setToolTipText(SystemResources.RESID_PREF_SIGNON_USERID_TOOLTIP);
		((GridData) userID.getLayoutData()).widthHint = 75;
		
		// New password
		label = new Label(c, SWT.NONE);
		label.setText(SystemResources.RESID_CHANGE_PASSWORD_NEW_LABEL);
		_txtNewPassword = SystemWidgetHelpers.createTextField(c, null);
		_txtNewPassword.setEchoChar('*');
		_txtNewPassword.setToolTipText(SystemResources.RESID_CHANGE_PASSWORD_NEW_TOOLTIP);
		((GridData) _txtNewPassword.getLayoutData()).widthHint = 75;
		
		// Confirm new password
		label = new Label(c, SWT.NONE);
		label.setText(SystemResources.RESID_CHANGE_PASSWORD_CONFIRM_LABEL);
		_txtConfirmPassword = SystemWidgetHelpers.createTextField(c, null);
		_txtConfirmPassword.setEchoChar('*');
		_txtConfirmPassword.setToolTipText(SystemResources.RESID_CHANGE_PASSWORD_CONFIRM_TOOLTIP);
		((GridData) _txtConfirmPassword.getLayoutData()).widthHint = 75;
		
	    // Save signon information checkbox
	    // DY:  align password checkbox with entry fields
	    _chkBoxSavePassword = SystemWidgetHelpers.createCheckBox(
	    	c, 1, this, SystemResources.RESID_PASSWORD_SAVE_LABEL, SystemResources.RESID_PASSWORD_SAVE_TOOLTIP);
	    _chkBoxSavePassword.setSelection(savePassword);
    	// disable until the user enters something for consistency with the save user ID checkbox
	    _chkBoxSavePassword.setEnabled(false);
	    
		_txtNewPassword.addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						newPasswordModified = true;
						validatePasswordInput();
					}
				}
			);
		
		_txtConfirmPassword.addModifyListener(
				new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						confirmModified = true;
						validatePasswordInput();
					}
				}
			);
		setInitialOKButtonEnabledState(false);
		
		return parent;
	}
	
    /**
     * Return true if the user elected to save the password
     */
    private boolean internalGetIsSavePassword()
    {
        savePassword = _chkBoxSavePassword.getSelection();
        return savePassword;
    }
    
  	/**
	 * This hook method is called whenever the text changes in the password input fields.	
	 */	
	protected SystemMessage validatePasswordInput() 
	{	
		clearErrorMessage();
		_chkBoxSavePassword.setEnabled(newPasswordModified || confirmModified);
		if (!newPasswordModified || !confirmModified) return null;
		okButton.setEnabled(true);
				
		String newPassword = _txtNewPassword.getText().trim();
		String confirmPassword = _txtConfirmPassword.getText().trim();
		
		if (!newPassword.equals(confirmPassword)) 
		{
			return RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_MISMATCH);
		}
		else if (newPassword.equals(""))
		{
			return RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD);
		}
		_newPassword = newPassword;
		return null;		
	}

	/**
	 * @see SystemPromptDialog#processOK()
	 */
	protected boolean processOK() 
	{
		savePassword = internalGetIsSavePassword();
		SystemMessage error = validatePasswordInput();
		if (error == null) return true;
		else setErrorMessage(error);
		return false;
	}
	
	/**
	 * @return The new password set by the user or null if the 
	 *         cancel button was pressed.
	 */
	public String getNewPassword() 
	{
		return _newPassword;		
	}
	
    /**
     * Return true if the user elected to make the changed user Id a permanent change.
     */
    public boolean getIsSavePassword()
    {
        return savePassword;
    }
    
    /**
     * Preselect the save password checkbox.  Default value is to not 
     * select the save password checkbox.
     */
    public void setSavePassword(boolean save)
    {
    	savePassword = save;
    }
}
