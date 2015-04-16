/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tm.terminal.view.core.preferences.ScopedEclipsePreferences;
import org.eclipse.tm.terminal.view.ui.local.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.local.showin.interfaces.IPreferenceKeys;

/**
 * Terminals default preferences initializer.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor.
	 */
	public PreferencesInitializer() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		ScopedEclipsePreferences prefs = UIPlugin.getScopedPreferences();

		prefs.putDefaultString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME);
		prefs.putDefaultString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX, null);
	}
}
