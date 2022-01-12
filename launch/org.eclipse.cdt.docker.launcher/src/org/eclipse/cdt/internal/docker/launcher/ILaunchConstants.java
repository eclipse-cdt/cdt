/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.debug.core.DebugPlugin;

public interface ILaunchConstants {

	public final static String LAUNCH_ID = DockerLaunchUIPlugin.getUniqueIdentifier() + ".launchConfigurationType"; //$NON-NLS-1$

	public final static String ATTR_ADDITIONAL_DIRS = DockerLaunchUIPlugin.getUniqueIdentifier() + ".additional_dirs"; //$NON-NLS-1$

	public final static String ATTR_EXPOSED_PORTS = DockerLaunchUIPlugin.getUniqueIdentifier() + ".exposed_ports"; //$NON-NLS-1$

	public final static String ATTR_IMAGE = DockerLaunchUIPlugin.getUniqueIdentifier() + ".image"; //$NON-NLS-1$

	public final static String ATTR_CONNECTION_URI = DockerLaunchUIPlugin.getUniqueIdentifier() + ".connection_uri"; //$NON-NLS-1$

	public final static String ATTR_KEEP_AFTER_LAUNCH = DockerLaunchUIPlugin.getUniqueIdentifier()
			+ ".keep_after_launch"; //$NON-NLS-1$

	public final static String ATTR_STDIN_SUPPORT = DockerLaunchUIPlugin.getUniqueIdentifier() + ".support_std_input"; //$NON-NLS-1$

	public final static String ATTR_PRIVILEGED_MODE = DockerLaunchUIPlugin.getUniqueIdentifier() + ".privileged_mode"; //$NON-NLS-1$

	// Attributes that need to match CDT attribute names
	public static final String ATTR_GDBSERVER_PORT = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_PORT"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_COMMAND"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT_DEFAULT = "2345"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND_DEFAULT = "gdbserver"; //$NON-NLS-1$

}
