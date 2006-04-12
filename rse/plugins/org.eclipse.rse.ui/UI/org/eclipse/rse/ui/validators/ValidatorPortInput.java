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


/**
 * For editable port number properties.
 * Ensures only digits are entered, and they appear to be a valid port.
 * TODO: what appears to be a valid port??
 */
public class ValidatorPortInput extends ValidatorIntegerInput 
{

	public static final int MAXIMUM_PORT_NUMBER = 65535; // according to IP specification
	
	public ValidatorPortInput()
	{
		super(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_EMPTY),
		      RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_NOTVALID));
	}

	/**
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object input) 
	{		
		String msg = super.isValid(input);
		if (msg == null)
		{
		  // yantzi: 2.1.2 added check for port > 65536 and changed msg to invalid port number
		  if (number < 0 || number > MAXIMUM_PORT_NUMBER)
		  {
		  //if (number <= 0) // we don't let user enter 0 explicitly, only via button selection.
		    //currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_EMPTY);
			currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_NOTVALID);
		    msg = currentMessage.getLevelOneText();
		  }
		}
		return msg;
	}

	/**
	 * @see org.eclipse.rse.ui.validators.ValidatorIntegerInput#isValid(java.lang.String)
	 */
	public String isValid(String input) {
		// yantzi:2.1.2 need to override this method in addition to the same 
		// one that takes Object parametere otherwise we get the wrong error messages!
		String msg = super.isValid(input);
		if (msg == null)
		{
		  // yantzi: 2.1.2 added check for port > 65536 and changed msg to invalid port number
		  if (number < 0 || number > MAXIMUM_PORT_NUMBER)
		  {
		  //if (number <= 0) // we don't let user enter 0 explicitly, only via button selection.
			//currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_EMPTY);
			currentMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_VALIDATE_PORT_NOTVALID);
			msg = currentMessage.getLevelOneText();
		  }		 
		}
		return msg;
	}

}