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
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Action to toggle the reverse video setting of all the GDB consoles
 */
public class GdbConsoleReverseVideoAction extends Action {
	
	public GdbConsoleReverseVideoAction() {
		super(ConsoleMessages.ConsoleReverseVideoAction_name);
		setText(ConsoleMessages.ConsoleReverseVideoAction_name);
		setToolTipText(ConsoleMessages.ConsoleReverseVideoAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_REVERSE_VIDEO));
	}
	
	@Override
	public void run() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		boolean enabled = preferences.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_REVERSE_VIDEO, false);
		preferences.putBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_REVERSE_VIDEO, !enabled);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
		}

		GdbConsoleManager manager = GdbUIPlugin.getGdbConsoleManager();
		for (GdbCliConsole console : manager.getCliConsoles()) {
			console.setReverseVideo(!enabled);
		}
	}
}
