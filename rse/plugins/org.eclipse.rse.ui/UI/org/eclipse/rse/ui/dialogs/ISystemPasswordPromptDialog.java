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
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Shell;

/**
 * Suggested interface for a dialog used to prompt user for a password.
 */
public interface ISystemPasswordPromptDialog 
{
	/**
	 * Set modal vs modeless
	 */
	public void setBlockOnOpen(boolean block);
	/**
	 * Open the dialog
	 */
	public int open();
	/**
	 * Set the input System object in which the user is attempting to do a connect action.
	 * This is used to query the system type, host name and userId to display to the user for
	 * contextual information.
	 * <p>
	 * This must be called right after instantiating this dialog.
	 */
	public void setSystemInput(IConnectorService systemObject);	
	/**
	 * Allow caller to determine if window was cancelled or not.
	 */
	public boolean wasCancelled();
	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v);
	/**
	 * Call this to specify a validator for the password. It will be called per keystroke.
	 */
	public void setPasswordValidator(ISystemValidator v);
	/**
	 * Call this to specify a validator for the signon.  It will be called when the OK button is pressed.
	 */
	public void setSignonValidator(ISignonValidator v);
	/**
	 * Call this to force the userId and password to uppercase
	 */
	public void setForceToUpperCase(boolean force);
	/**
	 * Call this to query the force-to-uppercase setting
	 */
	public boolean getForceToUpperCase();	
    /**
     * Return the userId entered by user
     */
    public String getUserId();
    /**
     * Return the password entered by user
     */
    public String getPassword();
    /**
     * Sets the password
     */
    public void setPassword(String password);
    /**
     * Preselect the save password checkbox.  Default value is to not 
     * select the save password checkbox.
     */
    public void setSavePassword(boolean save);
    /**
     * Return true if the user changed the user id
     */
    public boolean getIsUserIdChanged();
    /**
     * Return true if the user elected to make the changed user Id a permanent change.
     */
    public boolean getIsUserIdChangePermanent();    
    /**
     * Return true if the user elected to save the password
     */
    public boolean getIsSavePassword();    
    /**
     * Return the shell for this dialog
     */
    public Shell getShell();
}