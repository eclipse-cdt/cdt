/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.tcf.te.core.terminals.preferences.ScopedEclipsePreferences;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.interfaces.IPreferenceKeys;

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

		prefs.putDefaultBoolean(IPreferenceKeys.PREF_REMOVE_TERMINATED_TERMINALS, true);
	}
}
