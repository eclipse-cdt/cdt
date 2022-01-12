/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.internal.ui.preferences.CDebugPreferencePage;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Default preference value initializer for <code>CDebugUIplugin</code>.
 */
public class CDebugUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor for CDebugUIPreferenceInitializer.
	 */
	public CDebugUIPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore pstore = CDebugUIPlugin.getDefault().getPreferenceStore();
		CDebugPreferencePage.initDefaults(pstore);
		pstore.setDefault(ICDebugPreferenceConstants.PREF_DISASM_OPEN_NO_SOURCE_INFO, true);
		pstore.setDefault(ICDebugPreferenceConstants.PREF_DISASM_OPEN_SOURCE_NOT_FOUND, false);
		pstore.setDefault(ICDebugPreferenceConstants.PREF_DISASM_SHOW_INSTRUCTIONS, true);
		pstore.setDefault(ICDebugPreferenceConstants.PREF_DISASM_SHOW_SOURCE, true);
	}
}
