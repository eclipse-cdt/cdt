/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Access to indexer properties.
 * @since 4.0
 */
public class IndexerPreferences {
	public static final String KEY_INDEXER_ID= "indexerId"; //$NON-NLS-1$
	public static final String KEY_INDEX_ALL_FILES= "indexAllFiles"; //$NON-NLS-1$
	
	private static final String QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String INDEXER_NODE = "indexer"; //$NON-NLS-1$
	private static final String KEY_INDEXER_PREFS_SCOPE = "preferenceScope"; //$NON-NLS-1$

	public static final int SCOPE_INSTANCE = 0;
	public static final int SCOPE_PROJECT_PRIVATE = 1;
	public static final int SCOPE_PROJECT_SHARED = 2;

	/**
	 * Returns the scope that is selected for the project.
	 * @param project
	 * @return one of {@link #SCOPE_INSTANCE}, {@link #SCOPE_PROJECT_SHARED} or
	 * {@link #SCOPE_PROJECT_PRIVATE}.
	 */
	public static int getScope(IProject project) {
		int scope= SCOPE_INSTANCE;
		if (project != null) {
			Preferences ppp= getLocalPreferences(project);
			scope= ppp.getInt(KEY_INDEXER_PREFS_SCOPE, -1);
			if (scope == -1) {
				scope= determineScopeOnFirstUse(project);
			}
			if (scope != SCOPE_INSTANCE) {
				if (get(project, scope, KEY_INDEXER_ID, null) == null) {
					scope= SCOPE_INSTANCE;
					ppp.putInt(KEY_INDEXER_PREFS_SCOPE, scope);
					CCoreInternals.savePreferences(project);
				}
			}
		}
		return scope;
	}

	/**
	 * Sets the scope that shall be used for the project.
	 * Must be one of {@link #SCOPE_INSTANCE}, {@link #SCOPE_PROJECT_SHARED} or
	 * {@link #SCOPE_PROJECT_PRIVATE}.
	 */
	public static int setScope(IProject project, int scope) {
		if (project == null) 
			throw new IllegalArgumentException();
		boolean makeCopy= false;
		switch (scope) {
		case SCOPE_INSTANCE:
			break;
		case SCOPE_PROJECT_PRIVATE: 
		case SCOPE_PROJECT_SHARED:
			makeCopy= true;
			break;
		default:
			throw new IllegalArgumentException();
		}

		if (makeCopy) {
			Preferences[] prefs= getPreferences(project, scope);
			if (prefs[0].get(KEY_INDEXER_ID, null) == null) {
				Properties props= getProperties(project, SCOPE_INSTANCE);
				setProperties(prefs[0], props);
			}
		}
				
		Preferences ppp= getLocalPreferences(project);
		ppp.putInt(KEY_INDEXER_PREFS_SCOPE, scope);
		return scope;
	}

	/**
	 * Returns the properties for the indexer of a project.
	 */
	public static Properties getProperties(IProject project) {
		return getProperties(project, getScope(project));
	}

	/**
	 * Returns the properties for the indexer of a project for a
	 * specific scope.
	 */
	public static Properties getProperties(IProject project, int scope) {
		Preferences[] prefs= getPreferences(project, scope);
		Properties props= new Properties();
		for (int i=prefs.length-1; i>=0; i--) {
			addProperties(prefs[i], props);
		}
		return props;
	}
	
	/**
	 * Adds or changes indexer properties for a project.
	 */
	public static void setProperties(IProject project, int scope, Properties props) {
		Preferences[] prefs= getPreferences(project, scope);
		setProperties(prefs[0], props);
	}

	private static void setProperties(Preferences prefs, Properties props) {
		for (Iterator i = props.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			prefs.put(key, val);
		}
	}

	/**
	 * Returns an indexer property for the given project.
	 * @since 4.0
	 */
	public static String get(IProject project, String key, String defval) {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	Preferences[] prefs= IndexerPreferences.getPreferences(project);
    	return prefService.get(key, defval, prefs);
	}

	/**
	 * Returns an indexer property in a scope for the given project.
	 * @since 4.0
	 */
	private static String get(IProject project, int scope, String key, String defval) {
    	IPreferencesService prefService = Platform.getPreferencesService();
    	Preferences[] prefs= IndexerPreferences.getPreferences(project, scope);
    	return prefService.get(key, defval, prefs);
	}

	/**
	 * Adds or changes an indexer property for the given project.
	 */
	public static void set(final IProject project, String key, String value) {
		if (getScope(project) == SCOPE_INSTANCE) {
			setScope(project, SCOPE_PROJECT_PRIVATE);
		}
    	final Preferences[] prefs= IndexerPreferences.getPreferences(project.getProject());
    	prefs[0].put(key, value);
	}

	/**
	 * Sets up the initial indexing preferences for the project.
	 */
	private static int determineScopeOnFirstUse(IProject project) {
		int scope= SCOPE_INSTANCE;
		Preferences prjPrefs= getProjectPreferences(project);
		if (prjPrefs.get(KEY_INDEXER_ID, null) != null) {
			scope= SCOPE_PROJECT_SHARED;
		}
		else {
			Preferences oldStyle= prjPrefs.parent();
			String id= oldStyle.get(KEY_INDEXER_ID, null);
			if (id != null) {
				prjPrefs.put(KEY_INDEXER_ID, id);
				String value= oldStyle.get(KEY_INDEX_ALL_FILES, null);
				if (value != null) {
					prjPrefs.put(KEY_INDEX_ALL_FILES, value);
				}
				scope= SCOPE_PROJECT_SHARED;
			}
		}
		getLocalPreferences(project).putInt(KEY_INDEXER_PREFS_SCOPE, scope);
		CCoreInternals.savePreferences(project);
		return scope;
	}

	private static Preferences[] getPreferences(IProject project) {
		return getPreferences(project, getScope(project));
	}

	private static Preferences[] getPreferences(IProject project, int scope) {
		if (project != null) {
			switch (scope) {
			case SCOPE_PROJECT_PRIVATE:
				return new Preferences[] {getLocalPreferences(project)};
			case SCOPE_PROJECT_SHARED:
				return new Preferences[] {getProjectPreferences(project)};
			}
		}
		return getInstancePreferencesArray();
	}

	private static Preferences[] getInstancePreferencesArray() {
		return new Preferences[] {
				getInstancePreferences(),
				new ConfigurationScope().getNode(QUALIFIER).node(INDEXER_NODE), 
				new DefaultScope().getNode(QUALIFIER).node(INDEXER_NODE)
		};
	}

	private static Preferences getInstancePreferences() {
		return new InstanceScope().getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static Preferences getProjectPreferences(IProject project) {
		return new ProjectScope(project).getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static Preferences getLocalPreferences(IProject project) {
		return new LocalProjectScope(project).getNode(QUALIFIER).node(INDEXER_NODE);
	}
	
	private static void addProperties(Preferences preferences, Properties props) {
		try {
			String[] keys = preferences.keys();
			for (int i=0; i < keys.length; i++) {
				String key= keys[i];
				String val= preferences.get(key, null);
				if (val != null) {
					props.put(key, val);
				}
			}
		} catch (BackingStoreException e) {
		}
	}

	public static void initializeDefaultPreferences(IEclipsePreferences defaultPreferences) {
		Preferences prefs= defaultPreferences.node(INDEXER_NODE);
		prefs.put(KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		prefs.putBoolean(KEY_INDEX_ALL_FILES, false);
	}

	public static void addChangeListener(IProject prj, IPreferenceChangeListener pcl) {
		Preferences node= getProjectPreferences(prj);
		addListener(node, pcl);
		node= getLocalPreferences(prj);
		addListener(node, pcl);
		node= getInstancePreferences();
		addListener(node, pcl);
	}

	private static void addListener(Preferences node, IPreferenceChangeListener pcl) {
		if (node instanceof IEclipsePreferences) {
			IEclipsePreferences enode= (IEclipsePreferences) node;
			enode.addPreferenceChangeListener(pcl);
		}
	}

	public static void removeChangeListener(IProject prj, IPreferenceChangeListener pcl) {
		Preferences node= getProjectPreferences(prj);
		removeListener(node, pcl);
		node= getLocalPreferences(prj);
		removeListener(node, pcl);
		node= getInstancePreferences();
		removeListener(node, pcl);
	}

	private static void removeListener(Preferences node, IPreferenceChangeListener pcl) {
		if (node instanceof IEclipsePreferences) {
			IEclipsePreferences enode= (IEclipsePreferences) node;
			enode.removePreferenceChangeListener(pcl);
		}
	}
}
