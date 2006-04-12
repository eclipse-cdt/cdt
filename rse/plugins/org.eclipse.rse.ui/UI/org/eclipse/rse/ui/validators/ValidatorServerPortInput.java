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

import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;



public class ValidatorServerPortInput extends ValidatorPortInput
{
	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object input) 
	{		
		
		String msg = super.isValid(input);
		if (msg == null)
		{
		  	// check that it's not a used port
		  	if (number == 4035)
		  	{
		  		currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PORT_WARNING);
		  		currentMessage.makeSubstitution("4035", "RSE daemon");
		  		msg = currentMessage.getLevelOneText();
		  	}
		}
		return msg;
	}
	
	public String isValid(String input) 
	{
		// yantzi:2.1.2 need to override this method in addition to the same 
		// one that takes Object parametere otherwise we get the wrong error messages!
		String msg = super.isValid(input);
		if (msg == null)
		{		 		  	
		  	// check that it's not a used port
		  	if (number == 4035)
		  	{
		  		currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PORT_WARNING);
		  		currentMessage.makeSubstitution("4035", "RSE daemon");
		  		msg = currentMessage.getLevelOneText();
		  	}
		}
		return msg;
	}

}