/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.rse.core.IRSEPreferenceNames;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;

public class RSEPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		// the complex preferences
		RSEPreferencesManager.initDefaults();
		
		// the simple preferences
		Preferences prefs = RSECorePlugin.getDefault().getPluginPreferences();
		// The ID of the default persistence provider
		prefs.setDefault(IRSEPreferenceNames.DEFAULT_PERSISTENCE_PROVIDER, "org.eclipse.rse.persistence.MetadataPropertyFileProvider"); //$NON-NLS-1$
		// whether or not to create a local connection in a fresh workspace
		prefs.setDefault(IRSEPreferenceNames.CREATE_LOCAL_CONNECTION, true);
	}

}
