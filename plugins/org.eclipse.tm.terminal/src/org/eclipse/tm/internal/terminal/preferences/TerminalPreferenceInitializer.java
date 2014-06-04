/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;

public class TerminalPreferenceInitializer extends AbstractPreferenceInitializer {

	public TerminalPreferenceInitializer() {
	}

	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(TerminalPlugin.PLUGIN_ID);
		defaultPrefs.putBoolean(ITerminalConstants.PREF_INVERT_COLORS, ITerminalConstants.DEFAULT_INVERT_COLORS);
		defaultPrefs.putInt(ITerminalConstants.PREF_BUFFERLINES, ITerminalConstants.DEFAULT_BUFFERLINES);
        migrateTerminalPreferences();
	}

	/**
	 * Migrate settings from the older org.eclipse.tm.terminal.view bundle into the o.e.tm.terminal bundle 
	 */
	public static void migrateTerminalPreferences() {
		//InstanceScope.INSTANCE was only added in Eclipse 3.7 - we want to be compatible further back
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(TerminalPlugin.PLUGIN_ID);
		if (!prefs.getBoolean(ITerminalConstants.PREF_HAS_MIGRATED, false)) {
			prefs.putBoolean(ITerminalConstants.PREF_HAS_MIGRATED, true);
			PreferenceModifyListener.migrateTerminalPreferences(InstanceScope.INSTANCE.getNode("")); //$NON-NLS-1$
		}
	}

}
