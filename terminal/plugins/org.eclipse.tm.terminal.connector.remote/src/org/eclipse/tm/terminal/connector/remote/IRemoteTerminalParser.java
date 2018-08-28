/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.remote;

import java.io.IOException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;

public interface IRemoteTerminalParser {
	/**
	 * Initialize the remote shell. This method will be called after the connection has been initialized. Implementors can assume
	 * that the connection is open when this is called.
	 * 
	 * @param connection
	 *            terminal shell connection
	 * @return IRemoteProcess a remote process corresponding to the remote shell
	 * @throws IOException
	 *             if the remote shell fails to start for some reason
	 */
	IRemoteProcess initialize(IRemoteConnection connection) throws IOException;

	/**
	 * Parse the input stream. This method will be called with a buffer of characters read from the input stream. If the method
	 * returns true, the characters will be displayed in the terminal view, otherwise they will be ignored.
	 * 
	 * @param buf
	 *            buffer containing characters from the terminal input stream
	 * @return true if the characters should be displayed in the terminal
	 */
	boolean parse(byte[] buf);
}
