/********************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)  [246406] [performance] Timeout waiting when loading SystemPreferencesManager$ModelChangeListener during startup
 ********************************************************************************/

package org.eclipse.rse.ui.messages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageFile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;

/**
 * A SystemUIMessageFile extends SystemMessageFile and makes it more compatible
 * with Eclipse
 */
public class SystemUIMessageFile extends SystemMessageFile {
	
	/**
	 * Factory method for constructing a SystemUIMessageFile that loads the message file synchronously. 
	 * If an error occurs when reading the message file DTD then that is logged.
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
				SystemBasePlugin.logError("Could not open message file DTD.", e); //$NON-NLS-1$
			}
		} else {
			SystemBasePlugin.logError("Could not find mesage file DTD."); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * Factory method for constructing a SystemUIMessageFile that loads the message file in a thread.
	 *  
	 * The difference between {@link #getMessageFile(String,InputStream)} and this method
	 * is that the former calls the constructor that loads the message file synchronously 
	 * while this one loads the message file in a worker thread.  The message file URL is 
	 * passed in here so that the opening of it's input stream can be deferred until the 
	 * time when the worker thread is started and able to load the message file.
	 * 
	 * @param messageFileName The name of the message file to load. 
	 * @param messageFileURL The URL to the message file.
	 * @return The message file that was constructed.
	 * 
	 * @since 3.1
	 */
	public static SystemUIMessageFile getMessageFile(String messageFileName,
			URL messageFileURL) {
		SystemUIMessageFile result = null;
		URL dtdURL = RSEUIPlugin.getDefault().getMessageFileDTD();
		if (dtdURL != null) {
			result = new SystemUIMessageFile(messageFileName,messageFileURL, dtdURL);
		} else {
			SystemBasePlugin.logError("Could not find mesage file DTD."); //$NON-NLS-1$
		}
		return result;
	}
	

	/**
	 * Constructor for synchronous loading of the message file.  
	 * 
	 * This is private because this is not to be called directly, but rather 
	 * from {@link #getMessageFile(String, InputStream)}.
	 */
	private SystemUIMessageFile(String messageFileName,
			InputStream messageFileStream, InputStream dtdStream) {
		super(messageFileName, messageFileStream, dtdStream);
	}

	/**
	 * Constructor for lazy loading of the message file.
	 * 
	 * This is private because this is not to be called directly, but rather 
	 * from {@link #getMessageFile(String, URL)}.
	 */
	private SystemUIMessageFile(String messageFileName,
			URL messageFileURL, URL dtdURL) {
		super(messageFileName, messageFileURL, dtdURL);
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