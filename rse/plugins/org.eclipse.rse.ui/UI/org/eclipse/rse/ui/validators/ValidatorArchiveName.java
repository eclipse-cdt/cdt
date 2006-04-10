/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.validators;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * @author mjberger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ValidatorArchiveName extends ValidatorFileName {

	protected SystemMessage msg_NotRegisteredArchive;
	
	public ValidatorArchiveName(Vector existingNameList) {
		super(existingNameList);
	}

	public ValidatorArchiveName(String[] existingNameList) {
		super(existingNameList);
	}

	public ValidatorArchiveName() {
		super();
	}

	// ---------------------------
	// Parent Overrides...
	// ---------------------------
	/**
	 * Validate each character. 
	 * Override of parent method.
	 * Override yourself to refine the error checking.
	 * Also checks to see if its a valid archive name.	 
	 */
	public SystemMessage isSyntaxOk(String newText)
	{
		msg_NotRegisteredArchive = SystemPlugin.getPluginMessage(MSG_VALIDATE_ARCHIVE_NAME);
		msg_NotRegisteredArchive.makeSubstitution(newText);
		IStatus rc = workspace.validateName(newText, IResource.FILE);
		if (rc.getCode() != IStatus.OK)
			return msg_Invalid;
		else if ((getMaximumNameLength() > 0) && // defect 42507
			(newText.length() > getMaximumNameLength()))
			return msg_Invalid; // TODO: PHIL. MRI. better message.             
		else if (!ArchiveHandlerManager.getInstance().isRegisteredArchive(newText))
			return msg_NotRegisteredArchive;
		return checkForBadCharacters(newText) ? null: msg_Invalid;
	}
}