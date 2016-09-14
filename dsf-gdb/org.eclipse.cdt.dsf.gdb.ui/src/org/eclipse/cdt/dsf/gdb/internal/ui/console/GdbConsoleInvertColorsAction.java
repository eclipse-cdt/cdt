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
 * Action to toggle the inverted colors setting of all the GDB consoles
 */
public class GdbConsoleInvertColorsAction extends Action {
	
	public GdbConsoleInvertColorsAction() {
		setText(ConsoleMessages.ConsoleInvertColorsAction_name);
		setToolTipText(ConsoleMessages.ConsoleInvertColorsAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_CONSOLE_INVERT_COLORS));
	}
	
	@Override
	public void run() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(GdbPlugin.PLUGIN_ID);
		boolean enabled = preferences.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS, false);
		preferences.putBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS, !enabled);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
		}
	}
}
