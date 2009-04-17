/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc.preferences;

import org.eclipse.cdt.core.lrparser.xlc.activator.XlcParserPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * TODO trigger the indexer?
 * 
 * @author Mike Kucera
 */
public class XlcLanguagePreferences  {

	private static final String QUALIFIER = XlcParserPlugin.PLUGIN_ID;
	private static final String XLC_PREFERENCES_NODE = "xlc.preferences";


	public static void initializeDefaultPreferences() {
		Preferences prefs = getDefaultPreferences();
		prefs.putBoolean(XlcPreferenceKeys.KEY_SUPPORT_VECTOR_TYPES, true);
	}
	
	public static void setProjectPreference(String key, String value, IProject project) {
		getProjectPreferences(project).put(key, value);
	}
	
	
	public static void setWorkspacePreference(String key, String value) {
		getWorkspacePreferences().put(key, value);
	}
	
	
	public static String getProjectPreference(String key, IProject project) {
		return getProjectPreferences(project).get(key, null);
	}
	

	public static String getWorkspacePreference(String key) {
		return getWorkspacePreferences().get(key, null);
	}
	
	public static String getDefaultPreference(String key) {
		return getDefaultPreferences().get(key, null);
	}
	

	

	/**
	 * Returns the preference for the given key.
	 * 
	 * @param project If null then just the workspace and default preferences will be checked.
	 */
	public static String getPreference(String key, IProject project) {
		Preferences[] prefs;
		if(project == null) {
			prefs = new Preferences[] {
				getWorkspacePreferences(),
				getDefaultPreferences()
			};
		}
		else {
			prefs = new Preferences[] {
				getProjectPreferences(project),
				getWorkspacePreferences(),
				getDefaultPreferences()
			};
		}
		
		return Platform.getPreferencesService().get(key, null, prefs);
	}
	
	
	
	private static Preferences getDefaultPreferences() {
		return getPreferences(new DefaultScope());
	}
	
	private static Preferences getWorkspacePreferences() {
		return getPreferences(new InstanceScope());
	}
	
	private static Preferences getProjectPreferences(IProject project) {
		return getPreferences(new ProjectScope(project));
	}

	private static Preferences getPreferences(IScopeContext scope) {
		return scope.getNode(QUALIFIER).node(XLC_PREFERENCES_NODE);
	}


	
}
