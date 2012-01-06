/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;


public class ProfileVersioner implements IProfileVersioner {
	
	public static final String CODE_FORMATTER_PROFILE_KIND= "CodeFormatterProfile"; //$NON-NLS-1$
	
	public static final int VERSION_1= 1; // < 20061106 (pre CDT 4.0M3)
	
	public static final int CURRENT_VERSION= VERSION_1;
	
	
	@Override
	public int getFirstVersion() {
	    return VERSION_1;
    }

	@Override
	public int getCurrentVersion() {
	    return CURRENT_VERSION;
    }
	
	/**
     * {@inheritDoc}
     */
    @Override
	public String getProfileKind() {
	    return CODE_FORMATTER_PROFILE_KIND;
    }

	@Override
	public void update(CustomProfile profile) {
		final Map<String, String> oldSettings= profile.getSettings();
		Map<String, String> newSettings= updateAndComplete(oldSettings, profile.getVersion());
		profile.setVersion(CURRENT_VERSION);
		profile.setSettings(newSettings);
	}
	
	public static int getVersionStatus(CustomProfile profile) {
		final int version= profile.getVersion();
		if (version < CURRENT_VERSION) 
			return -1;
		else if (version > CURRENT_VERSION)
			return 1;
		else 
			return 0;
	}
	
	public static void updateAndComplete(CustomProfile profile) {
		final Map<String, String> oldSettings= profile.getSettings();
		Map<String, String> newSettings= updateAndComplete(oldSettings, profile.getVersion());
		profile.setVersion(CURRENT_VERSION);
		profile.setSettings(newSettings);
	}
	
	public static Map<String, String> updateAndComplete(Map<String, String> oldSettings, int version) {
		final Map<String, String> newSettings= FormatterProfileManager.getDefaultSettings();
		
		switch (version) {
		    
		default:
		    for (Object element : oldSettings.keySet()) {
				    final String key= (String)element;
				    if (!newSettings.containsKey(key)) 
				        continue;
				    
				    final String value= oldSettings.get(key);
				    if (value != null) {
				        newSettings.put(key, value);
				    }
				}
		}
		return newSettings;
	}
 }
