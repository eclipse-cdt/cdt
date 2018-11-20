/*******************************************************************************
 * Copyright (c) 2006, 2016 PalmSource, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Ewa Matejska    (PalmSource) - Adapted from IGDBServerMILaunchConfigurationConstants
 * Anna Dushistova (MontaVista) - [181517][usability] Specify commands to be run before remote application launch
 *******************************************************************************/

package org.eclipse.cdt.launch.remote;

import org.eclipse.debug.core.DebugPlugin;

public interface IRemoteConnectionConfigurationConstants {

	public static final String ATTR_REMOTE_CONNECTION = DebugPlugin.getUniqueIdentifier() + ".REMOTE_TCP"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_PORT"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_COMMAND"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_OPTIONS = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_OPTIONS"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT_DEFAULT = "2345"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND_DEFAULT = "gdbserver"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_OPTIONS_DEFAULT = ""; //$NON-NLS-1$

	/*
	 * Generic Remote Path and Download options
	 * ATTR_REMOTE_PATH: Path of the binary on the remote.
	 * ATTR_SKIP_DOWNLOAD_TO_TARGET: true if download to remote is not desired.
	 */
	public static final String ATTR_REMOTE_PATH = DebugPlugin.getUniqueIdentifier() + ".ATTR_TARGET_PATH"; //$NON-NLS-1$
	public static final String ATTR_SKIP_DOWNLOAD_TO_TARGET = DebugPlugin.getUniqueIdentifier()
			+ ".ATTR_SKIP_DOWNLOAD_TO_TARGET"; //$NON-NLS-1$

	public static final String ATTR_PRERUN_COMMANDS = DebugPlugin.getUniqueIdentifier() + ".ATTR_PRERUN_CMDS"; //$NON-NLS-1$

}
