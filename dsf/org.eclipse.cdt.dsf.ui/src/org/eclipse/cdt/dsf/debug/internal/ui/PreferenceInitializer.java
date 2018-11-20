/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui;

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore prefs = DsfUIPlugin.getDefault().getPreferenceStore();

		/*
		 * Debug View
		 */
		prefs.setDefault(IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT, 10);
		prefs.setDefault(IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE, true);
		prefs.setDefault(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE, false);
		prefs.setDefault(IDsfDebugUIConstants.PREF_MIN_STEP_INTERVAL, 100);
	}
}
