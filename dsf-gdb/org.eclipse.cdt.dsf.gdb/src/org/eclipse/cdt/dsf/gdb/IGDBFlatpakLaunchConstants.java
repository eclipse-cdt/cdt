/*******************************************************************************
 * Copyright (c) 2020 Red Hat.
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
package org.eclipse.cdt.dsf.gdb;

import org.eclipse.debug.core.DebugPlugin;

/**
 * Flatpak debugging constants
 *
 * @since 6.0
 */
public interface IGDBFlatpakLaunchConstants {

	// Attributes that need to match CDT attribute names
	public static final String ATTR_GDBSERVER_PORT = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_PORT"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND = DebugPlugin.getUniqueIdentifier() + ".ATTR_GDBSERVER_COMMAND"; //$NON-NLS-1$

	public static final String ATTR_GDBSERVER_PORT_DEFAULT = "2345"; //$NON-NLS-1$
	public static final String ATTR_GDBSERVER_COMMAND_DEFAULT = "gdbserver"; //$NON-NLS-1$

}
