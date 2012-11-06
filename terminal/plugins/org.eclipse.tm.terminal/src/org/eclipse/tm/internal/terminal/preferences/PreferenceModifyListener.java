/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.osgi.service.prefs.Preferences;

public class PreferenceModifyListener extends
		org.eclipse.core.runtime.preferences.PreferenceModifyListener {

	public PreferenceModifyListener() {
		// Nothing to do
	}

	/**
	 * Intercept programmatic access to old Terminal Preferences such as "invert"
	 */
	public IEclipsePreferences preApply(IEclipsePreferences node) {
		migrateTerminalPreferences(node.node("instance")); //$NON-NLS-1$
		return super.preApply(node);
	}

	public static void migrateTerminalPreferences(Preferences node) {
		Preferences terminalPrefs = node.node(TerminalPlugin.PLUGIN_ID);
		Preferences oldPrefs = node.node("org.eclipse.tm.terminal.view"); //$NON-NLS-1$
		String oldInvert = oldPrefs.get(ITerminalConstants.PREF_INVERT_COLORS, null);
		String oldBuflines = oldPrefs.get(ITerminalConstants.PREF_BUFFERLINES, null);
		if (oldInvert != null) {
			terminalPrefs.put(ITerminalConstants.PREF_INVERT_COLORS, oldInvert);
			oldPrefs.remove(ITerminalConstants.PREF_INVERT_COLORS);
		}
		if (oldBuflines != null) {
			terminalPrefs.put(ITerminalConstants.PREF_BUFFERLINES, oldBuflines);
			oldPrefs.remove(ITerminalConstants.PREF_BUFFERLINES);
		}
	}

}
