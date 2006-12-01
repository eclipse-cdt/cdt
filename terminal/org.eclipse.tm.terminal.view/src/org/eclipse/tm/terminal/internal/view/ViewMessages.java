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
package org.eclipse.tm.terminal.internal.view;

import org.eclipse.osgi.util.NLS;
public class ViewMessages extends NLS {
	static {
		NLS.initializeMessages(ViewMessages.class.getName(), ViewMessages.class);
	}
    public static String PROP_TITLE;
    public static String SETTINGS;

    public static String TERMINALSETTINGS;
    public static String CONNECTIONTYPE;

    public static String LIMITOUTPUT;
    public static String BUFFERLINES;
    public static String SERIALTIMEOUT;
    public static String NETWORKTIMEOUT;

	public static String STATE_CONNECTED;
	public static String STATE_CONNECTING;
	public static String STATE_OPENED;
	public static String STATE_CLOSED;

}
