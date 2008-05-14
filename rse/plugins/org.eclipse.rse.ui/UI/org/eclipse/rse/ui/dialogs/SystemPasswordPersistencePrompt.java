/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * David McKnight   (IBM)        - [223103] [cleanup] fix broken externalized strings
 * David Dykstal (IBM) - [232066] Adjusted width of dialog for translation
 *******************************************************************************/

package org.eclipse.rse.ui.dialogs;

import java.util.List;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * SystemPasswordPersistencePrompt is used with the save password preference page
 * to prompt the user to add or change password information.
 */
public final class SystemPasswordPersistencePrompt extends SystemPromptDialog implements ModifyListener
{



	private Text hostname, userid, password, passwordVerify;
	private Combo systemType;
	private IRSESystemType[] systemTypes;
	private SystemSignonInformation signonInfo;
	private boolean change;
	private String originalHostname, originalUserid;
	private IRSESystemType originalSystemType;
	
	private List existingEntries;
	
	/**
	 * Constructor for SystemPasswordPersistencePrompt.
	 * @param shell the parent shell
	 * @param title the dialog title
	 */
	public SystemPasswordPersistencePrompt(Shell shell, String title, List existingEntries, boolean change) {
		super(shell, title);
		this.change = change;
		this.existingEntries = existingEntries;
		setInitialOKButtonEnabledState(false);
	}


	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		
		Composite page = SystemWidgetHelpers.createComposite(parent, 2);
		Layout parentLayout = parent.getLayout();
		if (parentLayout instanceof GridLayout) {
			GridData gd = new GridData(SWT.BEGINNING, SWT.TOP, true, true);
			gd.widthHint = 400;
			page.setLayoutData(gd);
		}
		
		// Hostname prompt
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_HOSTNAME_LABEL);
		hostname = SystemWidgetHelpers.createTextField(page, null, SystemResources.RESID_PREF_SIGNON_HOSTNAME_TOOLTIP);
		if (originalHostname != null) 
			hostname.setText(originalHostname);
		hostname.addModifyListener(this);
				
		// System type prompt
		systemTypes = PasswordPersistenceManager.getInstance().getRegisteredSystemTypes();
		String[] systemTypeLabels = new String[systemTypes.length];
		for (int i=0; i<systemTypes.length; i++) {
			systemTypeLabels[i] = systemTypes[i].getLabel();
		}
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_SYSTYPE_LABEL, SystemResources.RESID_PREF_SIGNON_SYSTYPE_TOOLTIP);
		systemType = SystemWidgetHelpers.createReadonlyCombo(page, null);
		systemType.setItems(systemTypeLabels);
		if (originalSystemType != null)
			systemType.setText(originalSystemType.getLabel());
		systemType.addModifyListener(this);
		
		// User ID prompt
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_USERID_LABEL);
		userid = SystemWidgetHelpers.createTextField(page, null, SystemResources.RESID_PREF_SIGNON_USERID_TOOLTIP);
		if (originalUserid != null)
			userid.setText(originalUserid);
		userid.addModifyListener(this);
		
		// Password prompt
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_PASSWORD_LABEL);
		password = SystemWidgetHelpers.createTextField(page, null, SystemResources.RESID_PREF_SIGNON_PASSWORD_TOOLTIP);
		password.setEchoChar('*');
		password.addModifyListener(this);
		
		// Confirm password prompt
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_PREF_SIGNON_PASSWORD_VERIFY_LABEL);
		passwordVerify = SystemWidgetHelpers.createTextField(page, null,SystemResources.RESID_PREF_SIGNON_PASSWORD_VERIFY_TOOLTIP);
		passwordVerify.setEchoChar('*');
		passwordVerify.addModifyListener(this);
						
		return page;
	}

	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return hostname;
	}

	public SystemSignonInformation getSignonInformation() {
		return signonInfo;
	}
	
	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#processOK()
	 */
	protected boolean processOK() {
		// Check for blank fields
		String sHostName = hostname.getText();
		if (sHostName == null || sHostName.trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			okButton.setEnabled(false);
			hostname.setFocus();
			return false;
		}

		int systemTypeIndex = systemType.getSelectionIndex();
		if (systemTypeIndex<0)
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			okButton.setEnabled(false);
			systemType.setFocus();
			return false;
		}
		
		String sUserID = userid.getText();
		if (sUserID == null || sUserID.trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			okButton.setEnabled(false);
			userid.setFocus();
			return false;
		}

		String sPwd1 = password.getText();
		if (sPwd1 == null || sPwd1.trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			okButton.setEnabled(false);
			password.setFocus();
			return false;
		}

		String sPwd2 = passwordVerify.getText();
		if (sPwd2 == null || sPwd2.trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			okButton.setEnabled(false);
			passwordVerify.setFocus();
			return false;
		}
		
		// Check if new and verify passwords match
		if (!sPwd1.equals(sPwd2))
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_MISMATCH));
			okButton.setEnabled(false);
			password.setFocus();
			password.setSelection(0, sPwd1.length());
			return false;
		}

		IRSESystemType systemType = systemTypes[systemTypeIndex];
		signonInfo = new SystemSignonInformation(hostname.getText(), userid.getText(), password.getText(), systemType);
		
		if (change)
		{
			if (exists(signonInfo.getHostname(), signonInfo.getUserId(), signonInfo.getSystemType()))
			{
				if (!signonInfo.getSystemType().equals(originalSystemType) ||
					!signonInfo.getHostname().equalsIgnoreCase(originalHostname) ||
				    //!signonInfo.getHostname().equalsIgnoreCase(RSEUIPlugin.getQualifiedHostName(originalHostname)) ||
				    !signonInfo.getUserId().equals(originalUserid))
				{
					// User changed hostname, systemtype or userid and the change conflicts with an existing entry
					SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_EXISTS); 
					msg.makeSubstitution(sUserID, sHostName);
					setErrorMessage(msg);
					okButton.setEnabled(false);
					hostname.setFocus();
					return false;
				}
			}
		}
		else
		{
			// Adding a new entry, make sure it doesn't already exist
			if (exists(signonInfo.getHostname(), signonInfo.getUserId(), signonInfo.getSystemType()))
			{
				SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_EXISTS); 
				msg.makeSubstitution(sUserID, sHostName);
				setErrorMessage(msg);
				okButton.setEnabled(false);
				hostname.setFocus();
				return false;
			}
		}
		
		return super.processOK();
	}

	/**
	 * Check if a password is already saved for the given hostname, user ID and system type
	 */
	private boolean exists(String hostname, String userID, IRSESystemType systemType)
	{ 
		SystemSignonInformation info;
		PasswordPersistenceManager manager = PasswordPersistenceManager.getInstance();
		boolean found = false;
		
		for (int i = 0; !found && i < existingEntries.size(); i++)
		{
			info = (SystemSignonInformation) existingEntries.get(i);
			if (hostname.equalsIgnoreCase(info.getHostname()) &&
				systemType.equals(info.getSystemType()))
			{
				if (!manager.isUserIDCaseSensitive(info.getSystemType()))
				{
					found = userID.equalsIgnoreCase(info.getUserId());	
				}
				else
				{
					found = userID.equals(info.getUserId());
				}
			}
		}
		
		return found;
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		if (change)
		{
			SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "pwdi0002"); //$NON-NLS-1$
			password.setFocus();
		}
		else
		{
			SystemWidgetHelpers.setCompositeHelp(parent, RSEUIPlugin.HELPPREFIX + "pwdi0001"); //$NON-NLS-1$
			hostname.setFocus();
		}
	}

	/**
	 * Set the input data to prepopulate the change dialog
	 */
	public void setInputData(IRSESystemType systemtype, String hostname, String userid)
	{
		originalSystemType = systemtype;
		originalHostname = hostname;
		originalUserid = userid;
	}
	/**
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (e.getSource() == hostname && hostname.getText().trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			hostname.setFocus();
			okButton.setEnabled(false);
		}
		else if (e.getSource() == userid && userid.getText().trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			userid.setFocus();
			okButton.setEnabled(false);
		}
		else if (e.getSource() == systemType && systemType.getText().trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			systemType.setFocus();
			okButton.setEnabled(false);
		}
		else if (e.getSource() == password && password.getText().trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			password.setFocus();
			okButton.setEnabled(false);
		}
		else if (e.getSource() == passwordVerify && passwordVerify.getText().trim().equals("")) //$NON-NLS-1$
		{
			setErrorMessage(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_BLANKFIELD));
			passwordVerify.setFocus();
			okButton.setEnabled(false);
		}
		else
		{
			clearErrorMessage();

			if (hostname.getText().trim().equals("") ||  //$NON-NLS-1$
				userid.getText().trim().equals("") || //$NON-NLS-1$
				systemType.getText().trim().equals("") || //$NON-NLS-1$
				password.getText().trim().equals("") || //$NON-NLS-1$
				passwordVerify.getText().trim().equals("")) //$NON-NLS-1$
			{
				// clear error messages but button stays disabled
				okButton.setEnabled(false);
			}
			else
			{
				okButton.setEnabled(true);
			}
		}			
		
	}

	/**
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open()
	{
		return super.open();
	}

}
