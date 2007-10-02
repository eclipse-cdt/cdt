/********************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - created and used RSEPreferencesManager
 *                     - moved SystemPreferencesManager to a new plugin
 * Kevin Doyle (IBM) - [197199] Renaming a Profile doesn't cause a save
 * Yu-Fen Kuo (MontaVista) - [189271] [team] New Profile's are always active
 *                         - [189219] [team] Inactive Profiles become active after workbench restart
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.persistence.IRSEPersistenceProvider;

/**
 * A class that manages a list of SystemProfile objects.
 * This should be used as a singleton.
 */
public class SystemProfileManager implements ISystemProfileManager {

	private List _profiles = new ArrayList(10);
	private static SystemProfileManager singleton = null;
	private boolean restoring = false;

	/**
	 * Ordinarily there should be only one instance of a SystemProfileManager
	 * created on the system, so the static method {@link #getDefault()} is 
	 * preferred to using this.
	 */
	private SystemProfileManager() {
	}

	/**
	 * @return (and create if necessary) the singleton instance of this class.
	 */
	public static SystemProfileManager getDefault() {
		if (singleton == null) {
			singleton = new SystemProfileManager();
			RSECorePlugin.getThePersistenceManager().restoreProfiles(5000);
			singleton.ensureDefaultPrivateProfile();
			singleton.ensureDefaultTeamProfile();
		}
		return singleton;
	}

	/**
	 * Clear the default after a team synchronization say
	 */
	public static void clearDefault() {
		singleton = null;
	}

	public void setRestoring(boolean flag) {
		restoring = flag;
	}

	/**
	 * Create a new profile with the given name, and add to the list.
	 * The name must be unique within the existing list.
	 * <p>
	 * The underlying folder is created in the file system.
	 * <p>
	 * @param name What to name this profile
	 * @param makeActive true if this profile is to be added to the active profile list.
	 * @return new profile, or null if name not unique.
	 * @see ISystemProfileManager#createSystemProfile(String, boolean)
	 */
	public ISystemProfile createSystemProfile(String name, boolean makeActive) {
		ISystemProfile existingProfile = getSystemProfile(name);
		if (existingProfile != null) {
			deleteSystemProfile(existingProfile, false); // replace the existing one with a new profile
		}
		ISystemProfile newProfile = internalCreateSystemProfile(name);
                newProfile.setActive(makeActive);
		if (makeActive) {
			RSEPreferencesManager.addActiveProfile(name);
		}
		newProfile.commit();
		return newProfile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#makeSystemProfileActive(org.eclipse.rse.core.model.ISystemProfile, boolean)
	 */
	public void makeSystemProfileActive(ISystemProfile profile, boolean makeActive) {
		boolean wasActive = isSystemProfileActive(profile.getName());
		if (wasActive && !makeActive)
			RSEPreferencesManager.deleteActiveProfile(profile.getName());
		else if (makeActive && !wasActive) 
		        RSEPreferencesManager.addActiveProfile(profile.getName());
		profile.setActive(makeActive);
		profile.commit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getSystemProfiles()
	 */
	public ISystemProfile[] getSystemProfiles() {
		return getSystemProfiles(!restoring);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getSystemProfileNames()
	 */
	public String[] getSystemProfileNames() {
		ISystemProfile[] profiles = getSystemProfiles();
		String[] profileNames = new String[profiles.length];
		for (int i = 0; i < profiles.length; i++) {
			ISystemProfile profile = profiles[i];
			profileNames[i] = profile.getName();
		}
		return profileNames;
//		if (profileNames == null) {
//			ISystemProfile[] profiles = getSystemProfiles();
//			profileNames = new String[profiles.length];
//			for (int idx = 0; idx < profiles.length; idx++)
//				profileNames[idx] = profiles[idx].getName();
//		}
//		return profileNames;
	}

//	/**
//	 * Something changed so invalide cache of profiles so it will be regenerated
//	 */
//	protected void invalidateCache() {
//		profileNames = null;
//		profileNamesVector = null;
//	}
//
	
	public ISystemProfile getSystemProfile(String name) {
		ISystemProfile result = null;
		for (Iterator z = _profiles.iterator(); z.hasNext();) {
			ISystemProfile p = (ISystemProfile) z.next();
			if (p.getName().equals(name)) {
				result = p;
				break;
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getSystemProfile(java.lang.String)
	 */
	private ISystemProfile getSystemProfileOld(String name) {
		ISystemProfile[] profiles = getSystemProfiles();
		if ((profiles == null) || (profiles.length == 0)) return null;
		ISystemProfile match = null;
		for (int idx = 0; (match == null) && (idx < profiles.length); idx++)
			if (profiles[idx].getName().equals(name)) match = profiles[idx];
		return match;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#renameSystemProfile(org.eclipse.rse.core.model.ISystemProfile, java.lang.String)
	 */
	public void renameSystemProfile(ISystemProfile profile, String newName) {
		boolean isActive = isSystemProfileActive(profile.getName());
		String oldName = profile.getName();
		profile.setName(newName);
		if (isActive) {
			RSEPreferencesManager.renameActiveProfile(oldName, newName);
		}
		// Commit the profile to reflect the name change
		RSECorePlugin.getThePersistenceManager().commitProfile(profile, 5000);
		// Delete the profile by the old name, which is done in a separate job.
		RSECorePlugin.getThePersistenceManager().deleteProfile(profile.getPersistenceProvider(), oldName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#deleteSystemProfile(org.eclipse.rse.core.model.ISystemProfile, boolean)
	 */
	public void deleteSystemProfile(ISystemProfile profile, boolean persist) {
		String oldName = profile.getName();
		boolean isActive = isSystemProfileActive(oldName);
		_profiles.remove(profile);
		/* FIXME in EMF the profiles are "owned" by the Resource, and only referenced by the profile manager,
		 * so just removing it from the manager is not enough, it must also be removed from its resource.
		 * No longer needed since EMF is not in use.
		 * Resource res = profile.eResource();
		 * if (res != null)
		 * res.getContents().remove(profile);
		 */
		if (isActive) RSEPreferencesManager.deleteActiveProfile(oldName);
//		invalidateCache();
		if (persist) {
			IRSEPersistenceProvider provider = profile.getPersistenceProvider();
			RSECorePlugin.getThePersistenceManager().deleteProfile(provider, oldName);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#cloneSystemProfile(org.eclipse.rse.core.model.ISystemProfile, java.lang.String)
	 */
	public ISystemProfile cloneSystemProfile(ISystemProfile profile, String newName) {
		ISystemProfile newProfile = createSystemProfile(newName, false);
		return newProfile;
	}
	
	public boolean isSystemProfileActive(String profileName) {
		ISystemProfile profile = getSystemProfile(profileName);
		return profile.isActive();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#isSystemProfileActive(java.lang.String)
	 */
	private boolean isSystemProfileActiveOld(String profileName) {
		String[] activeProfiles = getActiveSystemProfileNames();
		boolean match = false;
		for (int idx = 0; !match && (idx < activeProfiles.length); idx++) {
			if (activeProfiles[idx].equals(profileName)) match = true;
		}
		return match;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getActiveSystemProfiles()
	 */
	public ISystemProfile[] getActiveSystemProfiles() {
		List activeProfiles = new ArrayList();
		for (Iterator z = _profiles.iterator(); z.hasNext();) {
			ISystemProfile p = (ISystemProfile) z.next();
			if (p.isActive()) {
				activeProfiles.add(p);
			}
		}
		ISystemProfile[] result = new ISystemProfile[activeProfiles.size()];
		activeProfiles.toArray(result);
		return result;
//		String[] profileNames = getActiveSystemProfileNames();
//		ISystemProfile[] profiles = new ISystemProfile[profileNames.length];
//		for (int idx = 0; idx < profileNames.length; idx++) {
//			profiles[idx] = getOrCreateSystemProfile(profileNames[idx]);
//			((SystemProfile) profiles[idx]).setActive(true);
//		}
//		return profiles;
	}
	
	public String[] getActiveSystemProfileNames() {
		ISystemProfile[] profiles = getActiveSystemProfiles();
		String[] names = new String[profiles.length];
		for (int i = 0; i < profiles.length; i++) {
			ISystemProfile systemProfile = profiles[i];
			names[i] = systemProfile.getName();
		}
		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getActiveSystemProfileNames()
	 */
	private String[] getActiveSystemProfileNamesOld() {
		String[] activeProfileNames = RSEPreferencesManager.getActiveProfiles();
		// dy: defect 48355, need to sync this with the actual profile list.  If the user
		// imports old preference settings or does a team sync and a profile is deleted then
		// it is possible an active profile no longer exists.
		// String[] systemProfileNames = getSystemProfileNames();
		ISystemProfile[] systemProfiles = getSystemProfiles();
		boolean found;
		boolean found_team = false;
		boolean found_private = false;
		boolean changed = false;
		String defaultProfileName = RSEPreferencesManager.getDefaultPrivateSystemProfileName();

		for (int activeIdx = 0; activeIdx < activeProfileNames.length; activeIdx++) {
			// skip Team and Private profiles
			String activeProfileName = activeProfileNames[activeIdx];
			if (activeProfileName.equals(defaultProfileName)) {
				found_private = true;
			} else if (activeProfileName.equals(RSEPreferencesManager.getDefaultTeamProfileName())) {
				found_team = true;
			} else {
				found = false;
				for (int systemIdx = 0; systemIdx < systemProfiles.length && !found; systemIdx++) {
					if (activeProfileNames[activeIdx].equals(systemProfiles[systemIdx].getName())) {
						found = true;
					}
				}

				if (!found) {
					// The active profile no longer exists so remove it from the active list
					RSEPreferencesManager.deleteActiveProfile(activeProfileNames[activeIdx]);
					changed = true;
				}
			}
		}

		for (int systemIdx = 0; systemIdx < systemProfiles.length && !changed; systemIdx++) {
			boolean matchesBoth = false;
			String name = systemProfiles[systemIdx].getName();

			for (int activeIdx = 0; activeIdx < activeProfileNames.length && !matchesBoth; activeIdx++) {
				String aname = activeProfileNames[activeIdx];
				if (name.equals(aname)) {
					matchesBoth = true;
				}

			}
			if (!matchesBoth && found_private) {
				if (systemProfiles[systemIdx].isActive() || systemProfiles[systemIdx].isDefaultPrivate()) {
					RSEPreferencesManager.addActiveProfile(name);
					RSEPreferencesManager.deleteActiveProfile(RSECorePlugin.getLocalMachineName());
					activeProfileNames = RSEPreferencesManager.getActiveProfiles();
				}
			}
		}

		// the active profiles list needed to be changed because of an external update, also
		// check if Default profile needs to be added back to the list
		if (changed || !found_team || !found_private) {
			if (systemProfiles.length == 0) {
				// First time user, make sure default is in the active list, the only time it wouldn't
				// be is if the pref_store.ini was modified (because the user imported old preferences)
				if (!found_team) {
					RSEPreferencesManager.addActiveProfile(RSEPreferencesManager.getDefaultTeamProfileName());
					changed = true;
				}

				if (!found_private) {
					RSEPreferencesManager.addActiveProfile(RSECorePlugin.getLocalMachineName());
					changed = true;
				}
			} else {
				ISystemProfile defaultProfile = getDefaultPrivateSystemProfile();
				if (defaultProfile != null && !found_private) {
					RSEPreferencesManager.addActiveProfile(defaultProfile.getName());
					changed = true;
				}
			}

			if (changed) {
				activeProfileNames = RSEPreferencesManager.getActiveProfiles();
			}
		}

		return activeProfileNames;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getDefaultPrivateSystemProfile()
	 */
	public ISystemProfile getDefaultPrivateSystemProfile() {
		return getSystemProfile(RSEPreferencesManager.getDefaultPrivateSystemProfileName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getDefaultTeamSystemProfile()
	 */
	public ISystemProfile getDefaultTeamSystemProfile() {
		return getSystemProfile(RSEPreferencesManager.getDefaultTeamProfileName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemProfileManager#getSize()
	 */
	public int getSize() {
		return _profiles.size();
	}
	
	/**
	 * Adds a newly restored profile to this manager
	 * @param profile the profile to add
	 */
	public void addSystemProfile(ISystemProfile profile) {
		_profiles.add(profile);
		profile.setProfileManager(this);
		String name = profile.getName();
		if (profile.isActive()) {
			RSEPreferencesManager.addActiveProfile(name);
		}
	}

	private void createDefaultPrivateProfile() {
		String initProfileName = RSEPreferencesManager.getDefaultPrivateSystemProfileName();
		ISystemProfile profile = internalCreateSystemProfile(initProfileName);
		profile.setDefaultPrivate(true);
	}

	private ISystemProfile internalCreateSystemProfile(String name) {
		ISystemProfile profile = new SystemProfile();
		profile.setName(name);
		addSystemProfile(profile);
		return profile;
	}

	private void ensureDefaultPrivateProfile() {
		// Ensure that one Profile is the default Profile - defect 48995 NH	
		boolean defaultProfileExists = false;
		for (Iterator z = _profiles.iterator(); z.hasNext() && !defaultProfileExists;) {
			ISystemProfile profile = (ISystemProfile) z.next();
			defaultProfileExists = profile.isDefaultPrivate();
		}
		if (!defaultProfileExists) {
			// find one with the right name
			String defaultPrivateProfileName = RSEPreferencesManager.getDefaultPrivateSystemProfileName();
			for (Iterator z = _profiles.iterator(); z.hasNext() && !defaultProfileExists;) {
				ISystemProfile profile = (ISystemProfile) z.next();
				if (profile.getName().equals(defaultPrivateProfileName)) {
					profile.setDefaultPrivate(true);
					defaultProfileExists = true;
				}
			}
		}
		if (!defaultProfileExists) {
			// Find the first profile that is not the Team profile and make it the default private profile 	
			String defaultTeamProfileName = RSEPreferencesManager.getDefaultTeamProfileName();
			for (Iterator z = _profiles.iterator(); z.hasNext() && !defaultProfileExists;) {
				ISystemProfile profile = (ISystemProfile) z.next();
				if (!profile.getName().equals(defaultTeamProfileName)) {
					profile.setDefaultPrivate(true);
					defaultProfileExists = true;
				}
			}
		}
		if (!defaultProfileExists) {
			// If Team is the only profile - then put a message in the log and create the default private profile
			Logger logger = RSECorePlugin.getDefault().getLogger();
			logger.logWarning("Only one Profile Team exists - there is no Default Profile"); //$NON-NLS-1$
			createDefaultPrivateProfile();
		}
	}
	
	private void ensureDefaultTeamProfile() {
		String name = RSEPreferencesManager.getDefaultTeamProfileName();
		ISystemProfile teamProfile = getSystemProfile(name);
		if (teamProfile == null) {
			teamProfile = internalCreateSystemProfile(name);
		}
	}

	private ISystemProfile[] getSystemProfiles(boolean ensureDefaultPrivateProfileExists) {
		if (ensureDefaultPrivateProfileExists) {
			ensureDefaultPrivateProfile();
		}
		ISystemProfile[] result = new ISystemProfile[_profiles.size()];
		_profiles.toArray(result);
		return result;
	}

//	/**
//	 * Instantiate a user profile given its name.
//	 * @param userProfileName the name of the profile to find or create
//	 * @return the profile that was found or created.
//	 */
//	private ISystemProfile getOrCreateSystemProfile(String userProfileName) {
//		ISystemProfile userProfile = getSystemProfile(userProfileName);
//		if (userProfile == null) {
//			userProfile = internalCreateSystemProfile(userProfileName);
//		}
//		return userProfile;
//	}

}
