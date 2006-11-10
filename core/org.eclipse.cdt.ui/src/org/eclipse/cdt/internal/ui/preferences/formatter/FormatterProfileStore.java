/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;


public class FormatterProfileStore extends ProfileStore {

	/**
	 * Preference key where all profiles are stored
	 */
	private static final String PREF_FORMATTER_PROFILES= "org.eclipse.jdt.ui.formatterprofiles"; //$NON-NLS-1$
	
//	private final IProfileVersioner fProfileVersioner;
		
	public FormatterProfileStore(IProfileVersioner profileVersioner) {
		super(PREF_FORMATTER_PROFILES, profileVersioner);
//		fProfileVersioner= profileVersioner;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public List readProfiles(IScopeContext scope) throws CoreException {
	    List profiles= super.readProfiles(scope);
	    return profiles;
	}

	public static void checkCurrentOptionsVersion() {
		PreferencesAccess access= PreferencesAccess.getOriginalPreferences();
		ProfileVersioner profileVersioner= new ProfileVersioner();
		
		IScopeContext instanceScope= access.getInstanceScope();
		IEclipsePreferences uiPreferences= instanceScope.getNode(CUIPlugin.PLUGIN_ID);
		int version= uiPreferences.getInt(PREF_FORMATTER_PROFILES + VERSION_KEY_SUFFIX, 0);
		if (version >= profileVersioner.getCurrentVersion()) {
			return; // is up to date
		}
		try {
			List profiles= (new FormatterProfileStore(profileVersioner)).readProfiles(instanceScope);
			if (profiles == null) {
				profiles= new ArrayList();
			}
			ProfileManager manager= new FormatterProfileManager(profiles, instanceScope, access, profileVersioner);
			if (manager.getSelected() instanceof CustomProfile) {
				manager.commitChanges(instanceScope); // updates core options
			}
			uiPreferences.putInt(PREF_FORMATTER_PROFILES + VERSION_KEY_SUFFIX, profileVersioner.getCurrentVersion());
			savePreferences(instanceScope);
						
			IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i= 0; i < projects.length; i++) {
				IScopeContext scope= access.getProjectScope(projects[i]);
				if (manager.hasProjectSpecificSettings(scope)) {
					manager= new FormatterProfileManager(profiles, scope, access, profileVersioner);
					manager.commitChanges(scope); // updates JavaCore project options
					savePreferences(scope);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (BackingStoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	private static void savePreferences(final IScopeContext context) throws BackingStoreException {
		try {
			context.getNode(CUIPlugin.PLUGIN_ID).flush();
		} finally {
			context.getNode(CCorePlugin.PLUGIN_ID).flush();
		}
	}
}
