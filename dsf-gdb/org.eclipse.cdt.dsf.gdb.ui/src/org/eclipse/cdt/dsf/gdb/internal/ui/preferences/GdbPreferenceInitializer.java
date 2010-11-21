/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initialize the GDB preferences.
 */
public class GdbPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE, true);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_USE_INSPECTOR_HOVER, true);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING, true);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS, 100);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND, IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		store.setDefault(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT, IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
	}
}
