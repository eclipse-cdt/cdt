/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui; 

import org.eclipse.cdt.debug.internal.ui.preferences.CDebugPreferencePage;
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore pstore = CDebugUIPlugin.getDefault().getPreferenceStore();
		CDebugPreferencePage.initDefaults( pstore );
	}
}
