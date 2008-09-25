/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 *******************************************************************************/

package org.eclipse.rse.core.subsystems;

import java.util.Arrays;
import java.util.List;

/**
 * Server Launch Type Enum type.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients. Use
 *                the {@link #get(int)} or {@link #get(String)} factory methods
 *                instead.
 */
public final class ServerLaunchType {
	/**
	 * The '<em><b>Daemon</b></em>' literal value (value 0). The server
	 * code is to be launched by calling a daemon that is listening on a port.
	 * 
	 * @see #DAEMON_LITERAL
	 */
	public static final int DAEMON = 0;

	/**
	 * The '<em><b>Rexec</b></em>' literal value (value 1). The server code
	 * is to be launched using REXEC
	 * 
	 * @see #REXEC_LITERAL
	 */
	public static final int REXEC = 1;

	/**
	 * The '<em><b>Running</b></em>' literal value (value 2). The server
	 * code is to already running, and doesn't need to be launched.
	 * 
	 * @see #RUNNING_LITERAL
	 */
	public static final int RUNNING = 2;

	/**
	 * The '<em><b>Telnet</b></em>' literal value (value 3). The server
	 * code is to be launched using TELNET.
	 * 
	 * @see #TELNET_LITERAL
	 */
	public static final int TELNET = 3;

	/**
	 * The '<em><b>SSH</b></em>' literal value (value 4). The server code
	 * is to be launched using SSH.
	 * 
	 * @see #SSH_LITERAL
	 */
	public static final int SSH = 4;

	/**
	 * The '<em><b>FTP</b></em>' literal value (value 5). The server code
	 * is to be launched using FTP
	 * 
	 * @see #FTP_LITERAL
	 */
	public static final int FTP = 5;

	/**
	 * The '<em><b>HTTP</b></em>' literal value (value 6). The server code
	 * is to be launched using HTTP
	 * 
	 * @see #HTTP_LITERAL
	 */
	public static final int HTTP = 6;

	/**
	 * The '<em><b>Daemon</b></em>' literal object.
	 * The server code is to be launched by calling a daemon that is listening on a port.
	 * @see #DAEMON
	 */
	public static final ServerLaunchType DAEMON_LITERAL = new ServerLaunchType(DAEMON, "Daemon"); //$NON-NLS-1$

	/**
	 * The '<em><b>Rexec</b></em>' literal object. The server code is to be
	 * launched using REXEC.
	 * 
	 * @see #REXEC
	 */
	public static final ServerLaunchType REXEC_LITERAL = new ServerLaunchType(REXEC, "Rexec"); //$NON-NLS-1$

	/**
	 * The '<em><b>Running</b></em>' literal object.
	 * The server code is to already running, and doesn't need to be launched.
	 * @see #RUNNING
	 */
	public static final ServerLaunchType RUNNING_LITERAL = new ServerLaunchType(RUNNING, "Running"); //$NON-NLS-1$

	/**
	 * The '<em><b>Telnet</b></em>' literal object.
	 * The server code is to be launched using TELNET.
	 * @see #TELNET
	 */
	public static final ServerLaunchType TELNET_LITERAL = new ServerLaunchType(TELNET, "Telnet"); //$NON-NLS-1$

	/**
	 * The '<em><b>SSH</b></em>' literal object.
	 * If the meaning of '<em><b>SSH</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * @see #SSH
	 */
	public static final ServerLaunchType SSH_LITERAL = new ServerLaunchType(SSH, "SSH"); //$NON-NLS-1$

	/**
	 * The '<em><b>FTP</b></em>' literal object. The server code is to be
	 * launched using FTP.
	 * 
	 * @see #FTP
	 */
	public static final ServerLaunchType FTP_LITERAL = new ServerLaunchType(FTP, "FTP"); //$NON-NLS-1$

	/**
	 * The '<em><b>HTTP</b></em>' literal object. The server code is to be
	 * launched using HTTP.
	 * 
	 * @see #HTTP
	 */
	public static final ServerLaunchType HTTP_LITERAL = new ServerLaunchType(HTTP, "HTTP"); //$NON-NLS-1$

	/**
	 * An array of all the '<em><b>Server Launch Type</b></em>' enumerators.
	 */
	private static final ServerLaunchType[] VALUES_ARRAY = new ServerLaunchType[] { DAEMON_LITERAL, REXEC_LITERAL, RUNNING_LITERAL, TELNET_LITERAL, SSH_LITERAL, FTP_LITERAL, HTTP_LITERAL, };

	private String _name;
	private int _value;

	/**
	 * A public read-only list of all the '<em><b>Server Launch Type</b></em>' enumerators.
	 */
	public static final List VALUES = Arrays.asList(VALUES_ARRAY);

	/**
	 * Returns the '<em><b>Server Launch Type</b></em>' literal with the specified name.
	 */
	public static ServerLaunchType get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ServerLaunchType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Server Launch Type</b></em>' literal with the specified value.
	 */
	public static ServerLaunchType get(int value) {
		switch (value) {
		case DAEMON:
			return DAEMON_LITERAL;
		case REXEC:
			return REXEC_LITERAL;
		case RUNNING:
			return RUNNING_LITERAL;
		case TELNET:
			return TELNET_LITERAL;
		case SSH:
			return SSH_LITERAL;
		case FTP:
			return FTP_LITERAL;
		case HTTP:
			return HTTP_LITERAL;
		}
		return null;
	}

	public String getName() {
		return _name;
	}

	public int getType() {
		return _value;
	}

	private ServerLaunchType(int value, String name) {
		_name = name;
		_value = value;

	}

} //ServerLaunchType
