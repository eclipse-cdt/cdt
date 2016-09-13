/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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
		setText(ConsoleMessages.ConsoleAutoTerminateAction_name);
		setToolTipText(ConsoleMessages.ConsoleAutoTerminateAction_description);

		// initialize state
		setChecked(readState());
	}

	private boolean readState() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		boolean terminate = true;
		if (preferences != null) {
			terminate = preferences.getBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, true);
		}

		return terminate;
	}

	@Override
	public void run() {
		// Toggle and update the new state
		boolean terminate = !readState();
		setChecked(terminate);

		// Update preference store
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		if (preferences != null) {
			preferences.putBoolean(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB, terminate);

			try {
				preferences.flush();
			} catch (BackingStoreException e) {
			}
		}
	}
}
