/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.internal.serial;

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

}
