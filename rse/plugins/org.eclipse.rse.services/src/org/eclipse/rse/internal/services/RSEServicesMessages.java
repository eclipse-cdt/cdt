/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Anna Dushistova  (MontaVista) - [261478] Remove SshShellService, SshHostShell (or deprecate and schedule for removal in 3.2)
 *******************************************************************************/
package org.eclipse.rse.internal.services;

import org.eclipse.osgi.util.NLS;

public class RSEServicesMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.services.messages"; //$NON-NLS-1$

	private RSEServicesMessages() {
	}

	public static String Socket_timeout;
	public static String FILEMSG_OPERATION_FAILED;
	public static String FILEMSG_OPERATION_FAILED_DETAILS;

	public static String FILEMSG_SECURITY_VIOLATION;
	public static String FILEMSG_SECURITY_VIOLATION_DETAILS;
	
	public static String FILEMSG_FOLDER_NOT_EMPTY;
	public static String FILEMSG_FOLDER_NOT_EMPTY_DETAILS;


	public static String TerminalShellService_description;
	public static String TerminalShellService_name;


	static {
		NLS.initializeMessages(BUNDLE_NAME, RSEServicesMessages.class);
	}

}
