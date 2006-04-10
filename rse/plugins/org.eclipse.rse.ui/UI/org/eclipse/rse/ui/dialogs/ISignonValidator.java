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

import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.swt.widgets.Shell;


/**
 * Interace for providing a signon validator to the password prompt dialog.
 */
public interface ISignonValidator 
{
	
	/**
	 * Used by ISystemPasswordPromptDialog to verify if the password entered by the user 
	 * is correct.
	 * 
	 * @return null if the password is valid, otherwise a SystemMessage is returned that can 
	 * be displayed to the end user.
	 */
	public SystemMessage isValid(ISystemPasswordPromptDialog dialog, String userid, String password);
	
	/**
	 * Verify if persisted userid and password are still valid
	 * 
	 * @param Shell, if null the validator will run headless, if not null then the validator
	 * may use the shell to prompt the user (for example, if the password has expired.)
	 * 
	 * @return true if signonInfo contains a valid signon, false otherwise.
	 */	
	public boolean isValid(Shell shell, SystemSignonInformation signonInfo);
}