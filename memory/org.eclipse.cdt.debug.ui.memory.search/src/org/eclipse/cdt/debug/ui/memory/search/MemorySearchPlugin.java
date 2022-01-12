/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class MemorySearchPlugin extends AbstractUIPlugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.debug.ui.memory.search"; //$NON-NLS-1$

	private static MemorySearchPlugin plugin;

	public MemorySearchPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static MemorySearchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the unique identifier for this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	protected static void logError(String message, Exception e) {
		Status status = new Status(IStatus.ERROR, PLUGIN_ID, DebugException.INTERNAL_ERROR, message, e);

		getDefault().getLog().log(status);
	}
}
