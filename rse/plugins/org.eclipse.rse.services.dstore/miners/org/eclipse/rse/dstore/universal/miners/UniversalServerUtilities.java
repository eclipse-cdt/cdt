/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation. All rights reserved.
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
 * Noriaki Takatsu (IBM) - [220126] [dstore][api][breaking] Single process server for multiple clients
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 ********************************************************************************/

package org.eclipse.rse.dstore.universal.miners;

import org.eclipse.dstore.core.model.DataStore;

/**
 * Utilities for dstore servers.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UniversalServerUtilities {

	/**
	 * logInfo
	 * 
	 * @param minerName
	 * 
	 * @param message Message text to be logged.
	 * @since org.eclipse.rse.services.dstore 3.0
	 */
	public static void logInfo(String minerName, String message, DataStore dataStore)
	{
		dataStore.getClient().getLogger().logInfo(minerName, message);
	}

	/**
	 * logWarning
	 * 
	 * @param minerName
	 * @param message Message text to be logged.
	 * @since org.eclipse.rse.services.dstore 3.0
	 */
	public static void logWarning(String minerName, String message, DataStore dataStore)
	{
		dataStore.getClient().getLogger().logWarning(minerName, message);
	}

	/**
	 * logError
	 * 
	 * @param minerName
	 * @param message Message text to be logged.
	 * @param exception Exception that generated the error. Used to print a
	 *            stack trace.
	 * @since org.eclipse.rse.services.dstore 3.0
	 */
	public static void logError(String minerName, String message, Throwable exception, DataStore dataStore)
	{
		dataStore.getClient().getLogger().logError(minerName, message, exception);
	}

	/**
	 * logDebugMessage
	 * 
	 * @param minerName
	 * @param message Message text to be logged.
	 * @since org.eclipse.rse.services.dstore 3.0
	 */
	public static void logDebugMessage(String minerName, String message, DataStore dataStore)
	{
		dataStore.getClient().getLogger().logDebugMessage(minerName, message);
	}

}