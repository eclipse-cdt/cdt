/*******************************************************************************
 * Copyright (c) 2003, 2012 Wind River Systems, Inc. and others.
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
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 * Martin Oberhuber (Wind River) - [206917] Add validation for Terminal Settings
 * Martin Oberhuber (Wind River) - [262996] get rid of TerminalState.OPENED
 * Ahmet Alptekin (Tubitak) - [244405] Add a UI Control for setting the Terminal's encoding
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
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
    public static String NEW_TERMINAL_CONNECTION;
    public static String NEW_TERMINAL_VIEW;
    public static String CONNECTIONTYPE;
	public static String VIEW_TITLE;
	public static String VIEW_SETTINGS;
	public static String INVALID_SETTINGS;
	public static String ENCODING;
	public static String ENCODING_WITH_PARENTHESES;

	public static String STATE_CONNECTED;
	public static String STATE_CONNECTING;
	public static String STATE_CLOSED;

	public static String CANNOT_INITIALIZE;
	public static String CONNECTOR_NOT_AVAILABLE;
	public static String ENCODING_NOT_AVAILABLE;
}
