/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.logging;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.rse.logging.IRemoteSystemsLogging;
import org.eclipse.rse.logging.RemoteSystemsLoggingPlugin;

/**
 * This class initializes logging preferences.
 */
public class LoggingPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor.
	 */
	public LoggingPreferenceInitializer() {
		super();
	}

	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences prefs = RemoteSystemsLoggingPlugin.getDefault().getPluginPreferences();
		prefs.setDefault(IRemoteSystemsLogging.DEBUG_LEVEL, IRemoteSystemsLogging.LOG_ERROR);
	}
}