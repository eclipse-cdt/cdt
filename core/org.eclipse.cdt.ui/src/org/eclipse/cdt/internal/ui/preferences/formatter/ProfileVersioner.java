/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;


public class ProfileVersioner {
	
	public static final int VERSION_1= 1; // < 20040113 (includes M6)
	
	public static final int CURRENT_VERSION= VERSION_1;
	
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
		final Map oldSettings= profile.getSettings();
		Map newSettings= updateAndComplete(oldSettings, profile.getVersion());
		profile.setVersion(CURRENT_VERSION);
		profile.setSettings(newSettings);
	}
	
	public static Map updateAndComplete(Map oldSettings, int version) {
		final Map newSettings= ProfileManager.getDefaultSettings();
		
		switch (version) {
		    
		default:
		    for (final Iterator iter= oldSettings.keySet().iterator(); iter.hasNext(); ) {
		        final String key= (String)iter.next();
		        if (!newSettings.containsKey(key)) 
		            continue;
		        
		        final String value= (String)oldSettings.get(key);
		        if (value != null) {
		            newSettings.put(key, value);
		        }
		    }
		}
		return newSettings;
	}
 }
