/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Initializes default values for LLDB preferences.
 */
public class LLDBPreferenceInitializer extends AbstractPreferenceInitializer {

	private static final String XCODE_BUNDLED_LLDB_PATH = "/Applications/Xcode.app/Contents/Developer/usr/bin/lldb-mi"; //$NON-NLS-1$

	private static String getDefaultCommand() {
		if (Platform.getOS().equals(Platform.OS_MACOSX) && new File(XCODE_BUNDLED_LLDB_PATH).exists()) {
			return XCODE_BUNDLED_LLDB_PATH;
		}
		return ILLDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT;
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(LLDBCorePlugin.PLUGIN_ID);
		node.put(ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND, getDefaultCommand());
		node.putBoolean(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		node.put(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
	}

}
