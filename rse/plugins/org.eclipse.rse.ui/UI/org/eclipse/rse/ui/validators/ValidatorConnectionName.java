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

package org.eclipse.rse.ui.validators;
import java.util.Vector;

import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * This class is used in dialogs that prompt for a connection alias name.
 * Relies on Eclipse supplied method to test for folder name validity.
 * <p>
 * The IInputValidator interface is used by jface's
 * InputDialog class and numerous other platform and system classes.
 */
public class ValidatorConnectionName extends ValidatorFolderName implements ISystemMessages, ISystemValidator
{
	public static final int MAX_CONNECTIONNAME_LENGTH = 100; // arbitrary restriction due to defects
		
	/**
	 * Constructor. 
	 * @param existingNameList Vector of existing names (strings) in owning profile. Can be null if not a rename operation.
	 */
	public ValidatorConnectionName(Vector existingNameList)
	{
		super(existingNameList);
		super.setErrorMessages(SystemPlugin.getPluginMessage(MSG_VALIDATE_CONNECTIONNAME_EMPTY),
		                       SystemPlugin.getPluginMessage(MSG_VALIDATE_CONNECTIONNAME_NOTUNIQUE),
		                       SystemPlugin.getPluginMessage(MSG_VALIDATE_CONNECTIONNAME_NOTVALID));  
	}
	
	/**
	 * Validate the given connection name is not already used in any profile. This is too expensive
	 * to do per keystroke, so you should call this after as a final test. Note, this is a warning
	 * situation, not an error, as we assume we have already tested for the containing profile, and
	 * thus is a test for a match on a connection in a non-containing profile. This results in msg
	 * rseg1241 being presented to the user, and if he chooses No to not continue, we return false 
	 * here. You should stop processing on false. Else, we return true meaning everything is ok.   
	 */
	public static boolean validateNameNotInUse(String proposedName, Shell shell)
	{
		SystemMessage msg = null;
		Vector profileNames = SystemPlugin.getTheSystemProfileManager().getSystemProfileNamesVector();
		String profileName = null;
		for (int idx=0; (msg==null)&& (idx<profileNames.size()); idx++)
		{
			profileName = (String)profileNames.elementAt(idx);
			IHost[] conns = SystemPlugin.getTheSystemProfileManager().getSystemProfile(profileName).getHosts();
			for (int jdx=0; (msg==null) && (jdx<conns.length); jdx++)
			{				
				if (conns[jdx].getAliasName().equalsIgnoreCase(proposedName))
					msg = SystemPlugin.getPluginMessage(MSG_VALIDATE_CONNECTIONNAME_NOTUNIQUE_OTHERPROFILE);
			}
		}
		if (msg != null)
		{
			msg.makeSubstitution(proposedName, profileName);
			SystemMessageDialog dlg = new SystemMessageDialog(shell, msg);
			boolean yesToContinue = dlg.openQuestionNoException();
			if (yesToContinue)
				msg = null; 
		}
		return (msg==null);
	}

    // ---------------------------
    // ISystemValidator methods...
    // ---------------------------
    
    /**
     * Return the max length for connections: 100
     */
    public int getMaximumNameLength()
    {
    	return MAX_CONNECTIONNAME_LENGTH;
    }
    
}