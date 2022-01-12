/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460496
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tm.terminal.view.core.preferences.ScopedEclipsePreferences;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.IPreferenceKeys;

/**
 * Terminal default preferences initializer.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	public PreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		ScopedEclipsePreferences prefs = UIPlugin.getScopedPreferences();

		prefs.putDefaultBoolean(IPreferenceKeys.PREF_REMOVE_TERMINATED_TERMINALS, true);

		prefs.putDefaultString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD,
				IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME);
		prefs.putDefaultString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX, null);
	}
}
