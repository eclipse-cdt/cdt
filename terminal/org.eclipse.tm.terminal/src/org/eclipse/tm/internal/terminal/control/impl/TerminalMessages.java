/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.impl;

import org.eclipse.osgi.util.NLS;

public class TerminalMessages extends NLS {
	static {
		NLS.initializeMessages(TerminalMessages.class.getName(), TerminalMessages.class);
	}

    public static String TerminalError;
    public static String SocketError;
    public static String IOError;
    public static String CannotConnectTo;
    public static String NotInitialized;

    //Preference Page
    public static String INVERT_COLORS;
    public static String BUFFERLINES;

}
