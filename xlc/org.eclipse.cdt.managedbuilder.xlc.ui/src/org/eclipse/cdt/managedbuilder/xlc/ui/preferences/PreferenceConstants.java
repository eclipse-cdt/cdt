/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.xlc.ui.preferences;

import org.eclipse.cdt.managedbuilder.xlc.ui.Messages;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_XL_COMPILER_ROOT = "XL_compilerRoot"; //$NON-NLS-1$

	public static final String P_XLC_COMPILER_VERSION = "XLC_compilerVersion"; //$NON-NLS-1$

	public static final String P_XL_COMPILER_VERSION_8 = "v8.0"; //$NON-NLS-1$
	public static final String P_XL_COMPILER_VERSION_9 = "v9.0"; //$NON-NLS-1$
	public static final String P_XL_COMPILER_VERSION_10 = "v10.1"; //$NON-NLS-1$
	public static final String P_XL_COMPILER_VERSION_11 = "v11.1"; //$NON-NLS-1$

	public static final String P_XL_COMPILER_VERSION_8_NAME = Messages.XLCompiler_v8;
	public static final String P_XL_COMPILER_VERSION_9_NAME = Messages.XLCompiler_v9;
	public static final String P_XL_COMPILER_VERSION_10_NAME = Messages.XLCompiler_v10;
	public static final String P_XL_COMPILER_VERSION_11_NAME = Messages.XLCompiler_v11;

	public static String getVersion(String label) {
		if (label.equalsIgnoreCase(P_XL_COMPILER_VERSION_11_NAME))
			return P_XL_COMPILER_VERSION_11;
		else if (label.equalsIgnoreCase(P_XL_COMPILER_VERSION_10_NAME))
			return P_XL_COMPILER_VERSION_10;
		else if (label.equalsIgnoreCase(P_XL_COMPILER_VERSION_9_NAME))
			return P_XL_COMPILER_VERSION_9;
		else
			return P_XL_COMPILER_VERSION_8;
	}

	public static String getVersionLabel(String version) {
		if (version.equalsIgnoreCase(P_XL_COMPILER_VERSION_11))
			return P_XL_COMPILER_VERSION_11_NAME;
		else if (version.equalsIgnoreCase(P_XL_COMPILER_VERSION_10))
			return P_XL_COMPILER_VERSION_10_NAME;
		else if (version.equalsIgnoreCase(P_XL_COMPILER_VERSION_9))
			return P_XL_COMPILER_VERSION_9_NAME;
		else
			return P_XL_COMPILER_VERSION_8_NAME;
	}

}
