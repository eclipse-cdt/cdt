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

import java.util.Collection;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.internal.useractions.Activator;
import org.eclipse.rse.internal.useractions.IUserActionsMessageIds;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorUniqueString;

/**
 * This class is used to verify a user defined compile command's label
 */
public class ValidatorCompileCommandLabel extends ValidatorUniqueString implements ISystemValidator {
	public static final int MAX_CMDLABEL_LENGTH = 50; // max name for a compile command name

	protected boolean fUnique;
	protected SystemMessage msg_Invalid;
	protected IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**
	 * Use this constructor when you have a list of existing labels.
	 * The collection will not be modified by the validator.
	 */
	public ValidatorCompileCommandLabel(Collection existingLabelList) {
		super(existingLabelList, CASE_INSENSITIVE); // case insensitive uniqueness
		init();
	}

	/**
	 * Use this constructor when you have an array of existing labels.
	 */
	public ValidatorCompileCommandLabel(String existingLabelList[]) {
		super(existingLabelList, CASE_INSENSITIVE); // case insensitive uniqueness
		init();
	}

	/**
	 * Use this constructor when the name need not be unique, and you just want
	 * the syntax checking. Or if you will call setExistingNamesList later.
	 */
	public ValidatorCompileCommandLabel() {
		super(new String[0], CASE_INSENSITIVE);
		init();
	}

	private void init() {
		String msg1Txt = UserActionsResources.MSG_VALIDATE_COMPILELABEL_EMPTY;
		String msg1Details = UserActionsResources.MSG_VALIDATE_COMPILELABEL_EMPTY_DETAILS;
		
		SystemMessage msg1 = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_VALIDATE_COMPILELABEL_EMPTY,
				IStatus.ERROR, msg1Txt, msg1Details);
		
		String msg2Txt = UserActionsResources.MSG_VALIDATE_COMPILELABEL_NOTUNIQUE;
		String msg2Details = UserActionsResources.MSG_VALIDATE_COMPILELABEL_NOTUNIQUE_DETAILS;
		
		SystemMessage msg2 = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_VALIDATE_COMPILELABEL_NOTUNIQUE,
				IStatus.ERROR, msg2Txt, msg2Details);
		
		super.setErrorMessages(msg1, msg2);
		fUnique = true;
		
		String msg3Txt = UserActionsResources.MSG_VALIDATE_COMPILELABEL_NOTVALID;
		String msg3Details= UserActionsResources.MSG_VALIDATE_COMPILELABEL_NOTVALID_DETAILS;
		
		msg_Invalid = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				IUserActionsMessageIds.MSG_VALIDATE_COMPILELABEL_NOTVALID,
				IStatus.ERROR, msg3Txt, msg3Details);
	}

	/**
	 * Supply your own error message text. By default, messages from RSEUIPlugin resource bundle are used.
	 * @param msg_Empty error message when entry field is empty
	 * @param msg_NonUnique error message when value entered is not unique
	 * @param msg_Invalid error message when syntax is not valid
	 */
	public void setErrorMessages(SystemMessage msg_Empty, SystemMessage msg_NonUnique, SystemMessage msg_Invalid) {
		super.setErrorMessages(msg_Empty, msg_NonUnique);
		this.msg_Invalid = msg_Invalid;
	}

	/**
	 * Overridable method for invalidate character check, beyond what this class offers
	 * @return true if valid, false if not
	 */
	protected boolean checkForBadCharacters(String newText) {
		return ((newText.indexOf('&') == -1) && // causes problems in menu popup as its a mnemonic character. 
		(newText.indexOf('@') == -1)); // defect 43950
	}

	public String toString() {
		return getClass().getName();
	}

	// ---------------------------
	// Parent Overrides...
	// ---------------------------
	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.	 
	 */
	public SystemMessage isSyntaxOk(String newText) {
		if (newText.length() > getMaximumNameLength())
			currentMessage = msg_Invalid;
		else
			currentMessage = checkForBadCharacters(newText) ? null : msg_Invalid;
		return currentMessage;
	}

	// ---------------------------
	// ISystemValidator methods...
	// ---------------------------

	/**
	 * Return the max length for compile commands: 50
	 */
	public int getMaximumNameLength() {
		return MAX_CMDLABEL_LENGTH;
	}

}
