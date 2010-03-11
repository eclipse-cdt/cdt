/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Noriaki Takatsu and Masao Nishimoto

 * Contributors:
 *  Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *  David McKnight  (IBM)  - [305272] [dstore][multithread] log close in ServerLogger
 *******************************************************************************/

package org.eclipse.dstore.core.server;

/**
 * @since 3.0
 */
public interface IServerLogger
{
	/**
	 * logInfo
	 *
	 * @param minerName
	 * @param message Message text to be logged.
	 */
	public void logInfo(String minerName, String message);

	/**
	 * logWarning
	 *
	 * @param minerName
	 * @param message Message text to be logged.
	 */
	public void logWarning(String minerName, String message);

	/**
	 * logError
	 *
	 * @param minerName
	 * @param message Message text to be logged.
	 * @param exception Exception that generated the error.  Used to print a stack trace.
	 */
	public void logError(String minerName, String message, Throwable exception);

	/**
	 * logDebugMessage
	 * @param minerName
	 * @param message Message text to be logged.
	 */
	public void logDebugMessage(String minerName, String message);
	
	/**
	 * closeLogFileStream
	 */
	public void closeLogFileStream();
}
