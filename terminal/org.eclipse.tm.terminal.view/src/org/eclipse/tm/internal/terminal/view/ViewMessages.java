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
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.osgi.util.NLS;

public class ViewMessages extends NLS {
	static {
		NLS.initializeMessages(ViewMessages.class.getName(), ViewMessages.class);
	}
    public static String NO_CONNECTION_SELECTED;
    public static String PROP_TITLE;
    public static String SETTINGS;

    public static String TERMINALSETTINGS;
    public static String CONNECTIONTYPE;
	public static String VIEW_TITLE;
	public static String VIEW_SETTINGS;

    public static String INVERT_COLORS;
    public static String BUFFERLINES;
    public static String SERIALTIMEOUT;
    public static String NETWORKTIMEOUT;

	public static String STATE_CONNECTED;
	public static String STATE_CONNECTING;
	public static String STATE_OPENED;
	public static String STATE_CLOSED;

	public static String CANNOT_INITIALIZE;
	public static String CONNECTOR_NOT_AVAILABLE;

}
