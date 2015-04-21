/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import org.eclipse.osgi.util.NLS;

public class TelnetMessages extends NLS {
	static {
		NLS.initializeMessages(TelnetMessages.class.getName(), TelnetMessages.class);
	}
	public static String PORT;
	public static String HOST;
	public static String CONNECTION_CLOSED_BY_FOREIGN_HOST;
	public static String TIMEOUT;

 }
