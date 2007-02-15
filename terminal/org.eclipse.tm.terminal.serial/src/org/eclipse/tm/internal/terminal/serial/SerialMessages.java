/*******************************************************************************
 * Copyright (c) 2003, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.serial;

import org.eclipse.osgi.util.NLS;
public class SerialMessages extends NLS {
	static {
		NLS.initializeMessages(SerialMessages.class.getName(), SerialMessages.class);
	}
    public static String CONNTYPE_SERIAL;
    public static String PROP_TITLE;
    public static String PORT;
    public static String BAUDRATE;
    public static String DATABITS;
    public static String STOPBITS;
    public static String PARITY;
    public static String FLOWCONTROL;
    public static String PORT_IN_USE;
    public static String TIMEOUT;

    public static String ERROR_LIBRARY_NOT_INSTALLED;

}
