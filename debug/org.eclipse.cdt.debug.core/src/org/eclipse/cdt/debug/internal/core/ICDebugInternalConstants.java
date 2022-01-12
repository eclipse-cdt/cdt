/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
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
	 * This preference is superseded by PREF_DEFAULT_SOURCE_CONTAINERS. Kept for backward
	 * compatibility only.
	 */
	public static final String PREF_COMMON_SOURCE_CONTAINERS = CDebugCorePlugin.getUniqueIdentifier()
			+ ".cDebug.common_source_containers"; //$NON-NLS-1$

	/**
	 * String preference for the default source containers.
	 */
	public static final String PREF_DEFAULT_SOURCE_CONTAINERS = CDebugCorePlugin.getUniqueIdentifier()
			+ ".cDebug.default_source_containers"; //$NON-NLS-1$

	/**
	 * Boolean preference indicating the on/off state of the Show Full Paths
	 * toggle action. Actually, as the action appears in multiple views, and the
	 * state is preserved for each view, the full preference key is the view ID
	 * plus this key
	 */
	public static final String SHOW_FULL_PATHS_PREF_KEY = "org.eclipse.cdt.debug.ui.cDebug.show_full_paths"; //$NON-NLS-1$

	/**
	 * An attribute set by a non-ICBreakpoint to support fullpath capability in the Breakpoints view.
	 * If this attribute exists, not <code>null</code>, in the breakpoint marker or the breakpoint is an
	 * ICBreakpoint type, then the show fullpath action in the Breakpoints view is enabled, otherwise
	 * disabled. The show fullpath action does not toggle the value of this breakpoint attribute, it is
	 * the breakpoint's responsibility to monitor the SHOW_FULL_PATHS_PREF_KEY value change and update
	 * the breakpoint presentation in the Breakpoints view.
	 */
	public static final String ATTR_CAPABLE_OF_SHOW_FULL_PATHS = "org.eclipse.cdt.debug.ui.cDebug.capable_of_show_full_paths"; //$NON-NLS-1$
}
