/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Rolland Liu (Blackberry Ltd.)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.Profile;


public class FormatterProfileStore extends ProfileStore {

	/**
	 * Preference key where all profiles are stored
	 */
	private static final String PREF_FORMATTER_PROFILES= "org.eclipse.cdt.ui.formatterprofiles"; //$NON-NLS-1$
	private static final String PREF_FORMATTER_PROFILES_OLD= "org.eclipse.jdt.ui.formatterprofiles"; //$NON-NLS-1$
	
	public FormatterProfileStore(IProfileVersioner profileVersioner) {
		super(PREF_FORMATTER_PROFILES, profileVersioner);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Profile> readProfiles(IScopeContext scope) throws CoreException {
		final IEclipsePreferences node= scope.getNode(CUIPlugin.PLUGIN_ID);
		final String profilesValue= node.get(PREF_FORMATTER_PROFILES_OLD, null);
		if (profilesValue != null) {
			// migrate to new preference key
			final String versionKeyOld = PREF_FORMATTER_PROFILES_OLD + VERSION_KEY_SUFFIX;
			String version= node.get(versionKeyOld, null);
			node.put(PREF_FORMATTER_PROFILES, profilesValue);
			node.put(PREF_FORMATTER_PROFILES + VERSION_KEY_SUFFIX, version);
			node.remove(PREF_FORMATTER_PROFILES_OLD);
			node.remove(versionKeyOld);
			try {
				node.flush();
			} catch (BackingStoreException exc) {
				return readProfilesFromString(profilesValue);
			}
		}
	    return super.readProfiles(scope);
	}

	public static void initAndCheckVersion() {
		PreferencesAccess access = PreferencesAccess.getOriginalPreferences();
		ProfileVersioner profileVersioner = new ProfileVersioner();

		IScopeContext instanceScope = access.getInstanceScope();
		IEclipsePreferences uiPreferences = instanceScope.getNode(CUIPlugin.PLUGIN_ID);
		int version = uiPreferences.getInt(PREF_FORMATTER_PROFILES + VERSION_KEY_SUFFIX, 0);
		if (version >= profileVersioner.getCurrentVersion()) {
			return; // is up to date
		}
		try {
			List<Profile> profiles = (new FormatterProfileStore(profileVersioner)).readProfiles(instanceScope);
			if (profiles == null) {
				profiles = new ArrayList<Profile>();
			}
			ProfileManager manager = new FormatterProfileManager(profiles, instanceScope, access,
					profileVersioner);
			Profile selectedProfile = manager.getSelected();
			if ((selectedProfile instanceof CustomProfile) || !profiles.contains(selectedProfile)) { // CustomProfile or Profile in plugin_customization.ini
				manager.commitChanges(instanceScope); // updates CDTCore options
			}
			uiPreferences.putInt(PREF_FORMATTER_PROFILES + VERSION_KEY_SUFFIX,
					profileVersioner.getCurrentVersion());
			savePreferences(instanceScope);

			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				IScopeContext scope = access.getProjectScope(projects[i]);
				if (manager.hasProjectSpecificSettings(scope)) {
					manager = new FormatterProfileManager(profiles, scope, access, profileVersioner);
					manager.commitChanges(scope); // updates CDTCore project options
					savePreferences(scope);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (BackingStoreException e) {
			CUIPlugin.log(e);
		}
	}

	private static void savePreferences(final IScopeContext context) throws BackingStoreException {
		try {
			context.getNode(CUIPlugin.PLUGIN_ID).flush();
		} finally {
			context.getNode(CUIPlugin.PLUGIN_CORE_ID).flush();
		}
	}

}
