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
 * David Dykstal (IBM) - [186589] move user types, user actions, and compile commands
 *                                API to the user actions plugin
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared                                
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.validators;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsMessageIds;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.validators.ISystemValidator;

/**
 * This class is used to verify a user defined action's command
 */
public class ValidatorUserActionCommand implements ISystemValidator {
	public static final int MAX_UDACMD_LENGTH = 512; // max command for an action

	protected SystemMessage emptyMsg, invalidMsg, currentMessage;

	/**
	 * Constructor to use when wanting to specify the "value required" error message,
	 * but use the default for the "Value not valid" error message
	 */
	public ValidatorUserActionCommand() {
		String msg1Txt = UserActionsResources.MSG_VALIDATE_UDACMD_EMPTY;
		String msg1Details = UserActionsResources.MSG_VALIDATE_UDACMD_EMPTY_DETAILS;
	
		SystemMessage msg1 = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_VALIDATE_UDACMD_EMPTY,
				IStatus.ERROR, msg1Txt, msg1Details);
		
		String msg2Txt = UserActionsResources.MSG_VALIDATE_UDACMD_NOTVALID;
		String msg2Details = UserActionsResources.MSG_VALIDATE_UDACMD_NOTVALID_DETAILS;
		
		SystemMessage msg2 = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_VALIDATE_UDACMD_NOTVALID,
				IStatus.ERROR, msg2Txt, msg2Details);
		setErrorMessages(msg1,msg2);
	}

	/**
	 * Set the error messages, overriding the defaults
	 */
	public void setErrorMessages(SystemMessage emptyMsg, SystemMessage invalidMsg) {
		this.emptyMsg = emptyMsg;
		this.invalidMsg = invalidMsg;
	}

	// ---------------------------
	// ISystemValidator methods...
	// ---------------------------    

	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object input) {
		currentMessage = null;
		if (!(input instanceof String)) {
			return null;
		} else
			return isValid((String) input);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 * @see #getSystemMessage()
	 */
	public String isValid(String input) {
		currentMessage = null;
		if ((input == null) || (input.length() == 0)) {
			currentMessage = emptyMsg;
		} else {
			if (input.length() > MAX_UDACMD_LENGTH) currentMessage = invalidMsg;
		}
		return (currentMessage == null) ? null : currentMessage.getLevelOneText();
	}

	/**
	 * When isValid returns non-null, call this to get the SystemMessage object for the error 
	 *  versus the simple string message.
	 */
	public SystemMessage getSystemMessage() {
		return currentMessage;
	}

	/**
	 * Return the max length for comments
	 */
	public int getMaximumNameLength() {
		return MAX_UDACMD_LENGTH;
	}

	/**
	 * For convenience, this is a shortcut to calling:
	 * <pre><code>
	 *  if (isValid(text) != null)
	 *    msg = getSystemMessage();
	 * </code></pre>
	 */
	public SystemMessage validate(String text) {
		if (isValid(text) != null)
			return currentMessage;
		else
			return null;
	}

}
