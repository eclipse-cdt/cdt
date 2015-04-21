/*******************************************************************************
 * Copyright (c) 2000, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 * Martin Oberhuber (Wind River) - [206919] Improve SSH Terminal Error Reporting (Adopting code from org.eclipse.team.cvs.core)
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.ssh.connector;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

public class SshMessages extends NLS {
	static {
		NLS.initializeMessages(SshMessages.class.getName(), SshMessages.class);
	}
	public static String USER;
	public static String HOST;
	public static String PORT;
	public static String PASSWORD;
	public static String TIMEOUT;
	public static String KEEPALIVE;
	public static String KEEPALIVE_Tooltip;
	public static String WARNING;
	public static String INFO;

	//These are from org.eclipse.team.cvs.ui.CVSUIMessages
	public static String UserValidationDialog_required;
	public static String UserValidationDialog_labelUser;
	public static String UserValidationDialog_labelPassword;
	public static String UserValidationDialog_password;
	public static String UserValidationDialog_user;
	public static String UserValidationDialog_5;
	public static String UserValidationDialog_6;
	public static String UserValidationDialog_7;

	public static String KeyboardInteractiveDialog_message;
	public static String KeyboardInteractiveDialog_labelConnection;

	public static String ERROR_CONNECTING;
	public static String TerminalCommunicationException_io;
	public static String SSH_AUTH_CANCEL;
	public static String SSH_AUTH_FAIL;
	public static String com_jcraft_jsch_JSchException;
	public static String java_io_IOException;
	public static String java_io_EOFException;
	public static String java_io_InterruptedIOException;
	public static String java_net_UnknownHostException;
	public static String java_net_ConnectException;
	public static String java_net_SocketException;
	public static String java_net_NoRouteToHostException;

    // <Copied from org.eclipse.team.cvs.core / CVSCommunicationException (c) IBM 2000, 2007>

	public static String getMessageFor(Throwable throwable) {
		String message = getMessage(getMessageKey(throwable));
		if (message == null) {
			message = NLS.bind(SshMessages.TerminalCommunicationException_io, (new Object[] { throwable.toString() }));
		} else {
			message = NLS.bind(message, (new Object[] { throwable.getMessage() }));
		}
		return message;
	}

	private static String getMessageKey(Throwable t) {
		String name = t.getClass().getName();
		name = name.replace('.', '_');
		return name;
	}

	// </Copied from org.eclipse.team.cvs.core / CVSCommunicationException>
	// <Copied from org.eclipse.team.cvs.core / Policy (c) IBM 2000, 2005>

	public static String getMessage(String key) {
		try {
			Field f = SshMessages.class.getDeclaredField(key);
			Object o = f.get(null);
			if (o instanceof String)
				return (String) o;
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	}

	// </Copied from org.eclipse.team.cvs.core / Policy>

 }
