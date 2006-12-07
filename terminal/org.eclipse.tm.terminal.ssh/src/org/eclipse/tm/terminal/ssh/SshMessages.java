/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.ssh;

import org.eclipse.osgi.util.NLS;

public class SshMessages extends NLS {
	static {
		NLS.initializeMessages(SshMessages.class.getName(), SshMessages.class);
	}
    public static String CONNTYPE;
	public static String USER;
	public static String HOST;
	public static String PORT;
	public static String PASSWORD;
	public static String TIMEOUT;
	public static String WARNING;
	public static String INFO;

 }
