/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Move to org.eclipse.cdt.dsf.gdb from UI plugin (bug 348159)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Initialize the GDB preferences.
 */
public class GdbPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE, true);
		node.putInt(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES, 500000);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_USE_INSPECTOR_HOVER, true);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING, true);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_USE_RTTI, true);
		node.putInt(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS, 100);
		node.put(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND, IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		node.put(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT, IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT);
		node.put(IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP, IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT, false);
		node.putInt(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE, IGdbDebugPreferenceConstants.COMMAND_TIMEOUT_VALUE_DEFAULT);
		node.putBoolean(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS, false);
	}
}
