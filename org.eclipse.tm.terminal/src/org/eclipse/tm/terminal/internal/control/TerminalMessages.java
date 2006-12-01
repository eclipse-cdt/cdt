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
package org.eclipse.tm.terminal.internal.control;

import org.eclipse.osgi.util.NLS;
public class TerminalMessages extends NLS {
	static {
		NLS.initializeMessages(TerminalMessages.class.getName(), TerminalMessages.class);
	}

    public static String TerminalError;
    public static String SocketError;
    public static String IOError;

}
