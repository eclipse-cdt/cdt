/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import org.eclipse.debug.core.DebugPlugin;

public interface ILaunchConstants {

	public final static String ATTR_ADDITIONAL_DIRS = Activator
			.getUniqueIdentifier() + ".additional_dirs"; //$NON-NLS-1$

	public final static String ATTR_COMMAND_DIR = Activator
			.getUniqueIdentifier() + ".command_dir"; //$NON-NLS-1$

	public final static String ATTR_IMAGE = Activator.getUniqueIdentifier()
			+ ".image"; //$NON-NLS-1$

	public final static String ATTR_CONNECTION_NAME = Activator
			.getUniqueIdentifier() + ".connection_name"; //$NON-NLS-1$

	public final static String ATTR_KEEP_AFTER_LAUNCH = Activator
			.getUniqueIdentifier() + ".keep_after_launch"; //$NON-NLS-1$

	public final static String ATTR_STDIN_SUPPORT = Activator
			.getUniqueIdentifier() + ".support_std_input"; //$NON-NLS-1$

	// Attributes that need to match CDT attribute names
	public static final String ATTR_GDBSERVER_PORT = DebugPlugin
			.getUniqueIdentifier() + ".ATTR_GDBSERVER_PORT"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND = DebugPlugin
			.getUniqueIdentifier() + ".ATTR_GDBSERVER_COMMAND"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT_DEFAULT = "2345"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND_DEFAULT = "gdbserver"; //$NON-NLS-1$

}
