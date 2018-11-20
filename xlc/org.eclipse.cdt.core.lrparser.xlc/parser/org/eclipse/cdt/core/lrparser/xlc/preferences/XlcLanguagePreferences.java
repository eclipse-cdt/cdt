/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class XlcLanguagePreferences {

	private static final String QUALIFIER = XlcParserPlugin.PLUGIN_ID;
	private static final String XLC_PREFERENCES_NODE = "xlc.preferences";

	static void initializeDefaultPreferences() {
		Preferences defaultNode = getDefaultPreferences();

		for (XlcPref p : XlcPref.values()) {
			defaultNode.put(p.toString(), p.getDefaultValue());
		}
	}

	public static void setProjectPreference(XlcPref key, String value, IProject project) {
		getProjectPreferences(project).put(key.toString(), value);
	}

	public static void setWorkspacePreference(XlcPref key, String value) {
		getWorkspacePreferences().put(key.toString(), value);
	}

	public static String getProjectPreference(XlcPref key, IProject project) {
		return getProjectPreferences(project).get(key.toString(), null);
	}

	public static String getWorkspacePreference(XlcPref key) {
		return getWorkspacePreferences().get(key.toString(), null);
	}

	public static String getDefaultPreference(XlcPref key) {
		return getDefaultPreferences().get(key.toString(), null);
	}

	/**
	 * Returns the preference for the given key.
	 *
	 * @param project If null then just the workspace and default preferences will be checked.
	 */
	public static String get(XlcPref key, IProject project) {
		return Platform.getPreferencesService().get(key.toString(), null, getPreferences(project));
	}

	private static Preferences[] getPreferences(IProject project) {
		if (project == null) {
			return new Preferences[] { getWorkspacePreferences(), getDefaultPreferences() };
		} else {
			return new Preferences[] { getProjectPreferences(project), getWorkspacePreferences(),
					getDefaultPreferences() };
		}
	}

	private static Preferences getDefaultPreferences() {
		return getPreferences(DefaultScope.INSTANCE);
	}

	private static Preferences getWorkspacePreferences() {
		return getPreferences(InstanceScope.INSTANCE);
	}

	private static Preferences getProjectPreferences(IProject project) {
		return getPreferences(new ProjectScope(project));
	}

	private static Preferences getPreferences(IScopeContext scope) {
		return scope.getNode(QUALIFIER).node(XLC_PREFERENCES_NODE);
	}

}
