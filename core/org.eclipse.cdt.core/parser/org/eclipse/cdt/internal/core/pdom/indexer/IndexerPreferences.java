/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.LocalProjectScope;
import org.eclipse.cdt.internal.core.pdom.IndexUpdatePolicy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Access to indexer properties.
 * @since 4.0
 */
public class IndexerPreferences {
	public static final int SCOPE_INSTANCE = 0;
	public static final int SCOPE_PROJECT_PRIVATE = 1;
	public static final int SCOPE_PROJECT_SHARED = 2;

	public static final int UPDATE_POLICY_IMMEDIATE= 0;
	public static final int UPDATE_POLICY_LAZY= 1;
	public static final int UPDATE_POLICY_MANUAL= 2;

	public static final String KEY_INDEXER_ID= "indexerId"; //$NON-NLS-1$
	public static final String KEY_INDEX_ALL_FILES= "indexAllFiles"; //$NON-NLS-1$
	public static final String KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG= "indexUnusedHeadersWithDefaultLang"; //$NON-NLS-1$
	public static final String KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG= "indexUnusedHeadersWithAlternateLang"; //$NON-NLS-1$
	public static final String KEY_INDEX_ON_OPEN= "indexOnOpen"; //$NON-NLS-1$
	public static final String KEY_INCLUDE_HEURISTICS= "useHeuristicIncludeResolution"; //$NON-NLS-1$
	public static final String KEY_SKIP_ALL_REFERENCES= "skipReferences"; //$NON-NLS-1$
	public static final String KEY_SKIP_IMPLICIT_REFERENCES= "skipImplicitReferences"; //$NON-NLS-1$
	public static final String KEY_SKIP_TYPE_REFERENCES= "skipTypeReferences"; //$NON-NLS-1$
	public static final String KEY_SKIP_MACRO_REFERENCES= "skipMacroReferences"; //$NON-NLS-1$
	public static final String KEY_UPDATE_POLICY= "updatePolicy"; //$NON-NLS-1$
	public static final String KEY_SKIP_FILES_LARGER_THAN_MB = "skipFilesLargerThanMB"; //$NON-NLS-1$

	private static final String KEY_INDEXER_PREFS_SCOPE = "preferenceScope"; //$NON-NLS-1$
	private static final String KEY_INDEX_IMPORT_LOCATION = "indexImportLocation"; //$NON-NLS-1$

	private static final String DEFAULT_INDEX_IMPORT_LOCATION = ".settings/cdt-index.zip"; //$NON-NLS-1$
	private static final int DEFAULT_UPDATE_POLICY= 0;
	public static final int DEFAULT_FILE_SIZE_LIMIT = 8;

	private static final String QUALIFIER = CCorePlugin.PLUGIN_ID;
	private static final String INDEXER_NODE = "indexer"; //$NON-NLS-1$


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
					CCoreInternals.savePreferences(project, false);
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
				Preferences ppp= getLocalPreferences(project);
				int oldScope= ppp.getInt(KEY_INDEXER_PREFS_SCOPE, SCOPE_INSTANCE);

				Properties props= getProperties(project, oldScope);
				setProperties(prefs[0], props);
			}
		}

		Preferences ppp= getLocalPreferences(project);
		ppp.putInt(KEY_INDEXER_PREFS_SCOPE, scope);
		return scope;
	}

	/**
	 * Sets the scope that shall be used for the project.
	 * Must be one of {@link #UPDATE_POLICY_IMMEDIATE}, {@link #UPDATE_POLICY_LAZY} or
	 * {@link #UPDATE_POLICY_MANUAL}.
	 */
	public static void setUpdatePolicy(IProject project, int policy) {
		switch (policy) {
		case UPDATE_POLICY_IMMEDIATE:
		case UPDATE_POLICY_LAZY:
		case UPDATE_POLICY_MANUAL:
			break;
		default:
			throw new IllegalArgumentException();
		}
		// no support for project specific policies.
		getInstancePreferences().put(KEY_UPDATE_POLICY, String.valueOf(policy));
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
		for (int i= 0; i < prefs.length; i++) {
			readProperties(prefs[i], props);
			if (i == 0) {
				migrateProperties(props);
			}
		}
		return props;
	}

	private static void migrateProperties(Properties props) {
		if (props.get(KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG) == null) {
			// backward compatibility
			if ("true".equals(props.get(KEY_INDEX_ALL_FILES))) { //$NON-NLS-1$
				props.put(KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "true"); //$NON-NLS-1$
			}
		}
	}

	public static Properties getDefaultIndexerProperties() {
		Preferences prefs= getDefaultPreferences();
		Properties props= new Properties();
		readProperties(prefs, props);
		return props;
	}

	public static int getDefaultUpdatePolicy() {
		Preferences[] prefs = new Preferences[] {
				getDefaultPreferences()
			};
		return getUpdatePolicy(prefs);
	}

	/**
	 * Adds or changes indexer properties for a project.
	 */
	public static void setProperties(IProject project, int scope, Properties props) {
		Preferences[] prefs= getPreferences(project, scope);
		setProperties(prefs[0], props);
	}

	private static void setProperties(Preferences prefs, Properties props) {
		for (Map.Entry<Object,Object> entry : props.entrySet()) {
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
		getLocalPreferences(project).putInt(KEY_INDEXER_PREFS_SCOPE, scope);
		CCoreInternals.savePreferences(project, false);
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
				getConfigurationPreferences(),
				getDefaultPreferences()
		};
	}

	private static Preferences getInstancePreferences() {
		return InstanceScope.INSTANCE.getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static Preferences getConfigurationPreferences() {
		return ConfigurationScope.INSTANCE.getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static Preferences getDefaultPreferences() {
		return DefaultScope.INSTANCE.getNode(QUALIFIER).node(INDEXER_NODE);
	}

	public static Preferences getProjectPreferences(IProject project) {
		return new ProjectScope(project).getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static Preferences getLocalPreferences(IProject project) {
		return new LocalProjectScope(project).getNode(QUALIFIER).node(INDEXER_NODE);
	}

	private static void readProperties(Preferences preferences, Properties props) {
		try {
			String[] keys = preferences.keys();
			for (String key : keys) {
				if (props.get(key) == null) {
					String val= preferences.get(key, null);
					if (val != null) {
						props.put(key, val);
					}
				}
			}
		} catch (BackingStoreException e) {
		}
	}

	public static void initializeDefaultPreferences(IEclipsePreferences defaultPreferences) {
		Preferences prefs= defaultPreferences.node(INDEXER_NODE);
		prefs.put(KEY_INDEXER_ID, IPDOMManager.ID_FAST_INDEXER);
		prefs.putBoolean(KEY_INDEX_ALL_FILES, true);
		prefs.putBoolean(KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, false);
		prefs.putBoolean(KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, false);
		prefs.putBoolean(KEY_INDEX_ON_OPEN, false);
		prefs.putBoolean(KEY_INCLUDE_HEURISTICS, true);
		prefs.putInt(KEY_SKIP_FILES_LARGER_THAN_MB, DEFAULT_FILE_SIZE_LIMIT);
		prefs.putBoolean(KEY_SKIP_ALL_REFERENCES, false);
		prefs.putBoolean(KEY_SKIP_IMPLICIT_REFERENCES, false);
		prefs.putBoolean(KEY_SKIP_TYPE_REFERENCES, false);
		prefs.putBoolean(KEY_SKIP_MACRO_REFERENCES, false);
		prefs.put(KEY_INDEX_IMPORT_LOCATION, DEFAULT_INDEX_IMPORT_LOCATION);
	}

	public static void setDefaultIndexerId(String defaultId) {
		getDefaultPreferences().put(KEY_INDEXER_ID, defaultId);
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

	public static String getIndexImportLocation(IProject project) {
		Preferences[] prefs;
		if (project != null) {
			prefs= new Preferences[] {
					getProjectPreferences(project),
					getInstancePreferences(),
					getConfigurationPreferences(),
					getDefaultPreferences()
			};
		} else {
			prefs= new Preferences[] {
					getInstancePreferences(),
					getConfigurationPreferences(),
					getDefaultPreferences()
			};
		}

		return Platform.getPreferencesService().get(KEY_INDEX_IMPORT_LOCATION, DEFAULT_INDEX_IMPORT_LOCATION, prefs);
	}

	public static void setIndexImportLocation(IProject project, String location) {
		if (!location.equals(getIndexImportLocation(project))) {
			getProjectPreferences(project).put(KEY_INDEX_IMPORT_LOCATION, location);
			CCoreInternals.savePreferences(project, true);
		}
	}

	public static int getUpdatePolicy(IProject project) {
		// no support for project specific policies
		Preferences[] prefs= getInstancePreferencesArray();
		return getUpdatePolicy(prefs);
	}

	private static int getUpdatePolicy(Preferences[] prefs) {
		String val= Platform.getPreferencesService().get(KEY_UPDATE_POLICY, null, prefs);
		if (val != null) {
			try {
				int result= Integer.parseInt(val);
				switch (result) {
				case IndexUpdatePolicy.POST_CHANGE:
				case IndexUpdatePolicy.POST_BUILD:
				case IndexUpdatePolicy.MANUAL:
					return result;
				}
			} catch (Exception e) {
				CCorePlugin.log(e);
			}
		}
		return DEFAULT_UPDATE_POLICY;
	}

	public static boolean preferDefaultLanguage(IProject project) {
		IPreferencesService prefService = Platform.getPreferencesService();
		Preferences[] prefs= IndexerPreferences.getPreferences(project);
		if ("true".equals(prefService.get(KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG, null, prefs)) && //$NON-NLS-1$
				"true".equals(prefService.get(KEY_INDEX_ALL_FILES, null, prefs)) && //$NON-NLS-1$
				!"true".equals(prefService.get(KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, null, prefs))) { //$NON-NLS-1$
			return false;
		}
		return true;
	}

	public static boolean preferDefaultLanguage(Properties props) {
		if ("true".equals(props.get(KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG)) && //$NON-NLS-1$
				"true".equals(props.get(KEY_INDEX_ALL_FILES)) && //$NON-NLS-1$
				!"true".equals(props.get(KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG))) { //$NON-NLS-1$
			return false;
		}
		return true;
	}
}
