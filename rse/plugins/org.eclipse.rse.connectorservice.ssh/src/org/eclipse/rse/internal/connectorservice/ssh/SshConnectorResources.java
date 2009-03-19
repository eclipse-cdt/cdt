/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - copy dialogs from team.cvs.ui
 * David McKnight (IBM) - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight (IBM) - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Johnson Ma (Wind River) - [218880] Add UI setting for ssh keepalives
 * Martin Oberhuber (Wind River) - [227135] Cryptic exception when sftp-server is missing
 *******************************************************************************/

package org.eclipse.rse.internal.connectorservice.ssh;

import org.eclipse.osgi.util.NLS;

public class SshConnectorResources extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.internal.connectorservice.ssh.SshConnectorResources"; //$NON-NLS-1$
	static {
		NLS.initializeMessages(BUNDLE_NAME, SshConnectorResources.class);
	}
	private SshConnectorResources() {
	}

	public static String SshConnectorService_Name;
	public static String SshConnectorService_Description;

	public static String SshConnectorService_ErrorDisconnecting;
	public static String SshConnectorService_Info;
	public static String SshConnectorService_Warning;
	public static String SshConnectorService_Missing_sshd;

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

	public static String SSH_SETTINGS_LABEL;
	public static String PROPERTY_LABEL_TIMEOUT;
	public static String PROPERTY_LABEL_KEEPALIVE;

}
