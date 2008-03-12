/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.ssh;

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

 }
