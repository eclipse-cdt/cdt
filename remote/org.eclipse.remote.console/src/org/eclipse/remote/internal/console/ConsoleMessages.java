/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import org.eclipse.osgi.util.NLS;

public class ConsoleMessages extends NLS {
	static {
		NLS.initializeMessages(ConsoleMessages.class.getName(), ConsoleMessages.class);
	}

	public static String SELECT_CONNECTION;
	public static String ENCODING_UNAVAILABLE_0;
	public static String ENCODING_UNAVAILABLE_1;
	public static String ENCODING;
	public static String OPEN_CONSOLE_ERROR;
	public static String STATUS;
	public static String STATUS_CONNECTED;
	public static String STATUS_CONNECTING;
	public static String STATUS_CLOSED;

	public static String CONNECTING_TO_TERMINAL;
	public static String OPENNING_TERMINAL;
	public static String MAKING_CONNECTION;
	public static String DISCONNECTING;
	public static String TerminalConsoleConnector_0;
}
