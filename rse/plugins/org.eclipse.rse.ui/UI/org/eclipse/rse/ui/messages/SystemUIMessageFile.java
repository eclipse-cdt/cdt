/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.messages;
import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;

/**
 * @author dmcknigh
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SystemUIMessageFile extends SystemMessageFile
{
	public SystemUIMessageFile(String messageFileName, 
					String defaultMessageFileLocation)
	{
		super(messageFileName, defaultMessageFileLocation);
	}
	
	/**
	 * Override this to provide different extended SystemMessage implementation
	 * @param componentAbbr
	 * @param subComponentAbbr
	 * @param msgNumber
	 * @param msgIndicator
	 * @param msgL1
	 * @param msgL2
	 * @return The SystemMessage for the given message information
	 * @throws IndicatorException
	 */
	protected SystemMessage loadSystemMessage(String componentAbbr, String subComponentAbbr, String msgNumber, char msgIndicator,
			String msgL1, String msgL2) throws IndicatorException
	{
		return new SystemUIMessage(componentAbbr, subComponentAbbr, msgNumber, msgIndicator, msgL1, msgL2);
	}
}