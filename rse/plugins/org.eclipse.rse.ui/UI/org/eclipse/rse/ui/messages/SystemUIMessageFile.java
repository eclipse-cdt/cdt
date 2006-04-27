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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * A SystemUIMessageFile extends SystemMessageFile and makes it more compatible
 * with Eclipse
 */
public class SystemUIMessageFile extends SystemMessageFile {
	
	/**
	 * Factory method for constructing a SystemUIMessageFile. If an error occurs when
	 * reading the message file DTD then that is logged.
	 * @param messageFileName The "registered" name of the message file. Used to determine
	 * if the message file has been loaded. 
	 * @param messageFileStream The stream containing the message file. It is the
	 * caller's responsibility to close this stream.
	 * @return The message file that was constructed.
	 */
	public static SystemUIMessageFile getMessageFile(String messageFileName,
			InputStream messageFileStream) {
		SystemUIMessageFile result = null;
		URL dtdURL = RSEUIPlugin.getDefault().getMessageFileDTD();
		if (dtdURL != null) {
			try {
				InputStream dtdStream = dtdURL.openStream();
				result = new SystemUIMessageFile(messageFileName,
						messageFileStream, dtdStream);
				dtdStream.close();
			} catch (IOException e) {
				RSEUIPlugin.logError("Could not open message file DTD.", e);
			}
		} else {
			RSEUIPlugin.logError("Could not find mesage file DTD.");
		}
		return result;
	}

	private SystemUIMessageFile(String messageFileName,
			InputStream messageFileStream, InputStream dtdStream) {
		super(messageFileName, messageFileStream, dtdStream);
	}

	/**
	 * Override this to provide different extended SystemMessage implementation
	 * 
	 * @param componentAbbr
	 * @param subComponentAbbr
	 * @param msgNumber
	 * @param msgIndicator
	 * @param msgL1
	 * @param msgL2
	 * @return The SystemMessage for the given message information
	 * @throws IndicatorException
	 */
	protected SystemMessage loadSystemMessage(String componentAbbr,
			String subComponentAbbr, String msgNumber, char msgIndicator,
			String msgL1, String msgL2) throws IndicatorException {
		return new SystemUIMessage(componentAbbr, subComponentAbbr, msgNumber,
				msgIndicator, msgL1, msgL2);
	}
}