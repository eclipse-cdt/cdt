/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Action to toggle the preference to terminate GDB when last process exits
 */
public class GdbAutoTerminateAction extends Action {

	public GdbAutoTerminateAction() {
		super(ConsoleMessages.ConsoleAutoTerminateAction_name, IAction.AS_CHECK_BOX);
		setToolTipText(ConsoleMessages.ConsoleAutoTerminateAction_description);

		// initialize state
		setChecked(readState());
	}

	private boolean readState() {
		return Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true, null);
	}

	@Override
	public void run() {
		// All we need to do is update the preference store.  There is no other
		// immediate action to take.
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		if (preferences != null) {
			preferences.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, !readState());

			try {
				preferences.flush();
			} catch (BackingStoreException e) {
			}
		}
	}
}
