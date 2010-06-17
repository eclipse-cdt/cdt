/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;

/**
 * Definitions of the internal constants for C/C++ Debug plug-in.
 */
public class ICDebugInternalConstants {

	/**
	 * Status handler codes.
	 */
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;

	/**
	 * String preference for the common source containers.
	 */
	public static final String PREF_COMMON_SOURCE_CONTAINERS = CDebugCorePlugin.getUniqueIdentifier() + ".cDebug.common_source_containers"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating the on/off state of the Show Full Paths
	 * toggle action. Actually, as the action appears in multiple views, and the
	 * state is preserved for each view, the full preference key is the view ID
	 * plus this key
	 */
	public static final String SHOW_FULL_PATHS_PREF_KEY = "org.eclipse.cdt.debug.ui.cDebug.show_full_paths"; //$NON-NLS-1$	
}
