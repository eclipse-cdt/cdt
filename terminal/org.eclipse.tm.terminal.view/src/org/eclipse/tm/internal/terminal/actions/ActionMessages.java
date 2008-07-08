/*******************************************************************************
 * Copyright (c) 2003, 2008 Wind River Systems, Inc. and others.
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
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {

	static {
		NLS.initializeMessages(ActionMessages.class.getName(), ActionMessages.class);
	}
    public static String  NEW_TERMINAL_CONNECTION;
    public static String  NEW_TERMINAL_VIEW;
    public static String  CONNECT;
    public static String  TOGGLE_COMMAND_INPUT_FIELD;
    public static String  DISCONNECT;
    public static String  SETTINGS_ELLIPSE;
    public static String  SCROLL_LOCK_0;
    public static String  SCROLL_LOCK_1;
	public static String  REMOVE;

    public static String ConsoleDropDownAction_0;
    public static String ConsoleDropDownAction_1;

    public static String  SETTINGS;

}
