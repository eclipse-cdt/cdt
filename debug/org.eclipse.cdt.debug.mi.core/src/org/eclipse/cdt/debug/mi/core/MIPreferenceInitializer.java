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
package org.eclipse.cdt.debug.mi.core; 

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 
public class MIPreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 * Constructor for MIPreferenceInitializer. 
	 */
	public MIPreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		MIPlugin.getDefault().getPluginPreferences().setDefault(IMIConstants.PREF_REQUEST_TIMEOUT, IMIConstants.DEF_REQUEST_TIMEOUT);
		MIPlugin.getDefault().getPluginPreferences().setDefault(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, IMIConstants.DEF_REQUEST_LAUNCH_TIMEOUT);
		MIPlugin.getDefault().getPluginPreferences().setDefault(IMIConstants.PREF_REGISTERS_AUTO_REFRESH, IMIConstants.DEF_PREF_REGISTERS_AUTO_REFRESH);
		MIPlugin.getDefault().getPluginPreferences().setDefault(IMIConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH, IMIConstants.DEF_PREF_SHARED_LIBRARIES_AUTO_REFRESH);
	}
}