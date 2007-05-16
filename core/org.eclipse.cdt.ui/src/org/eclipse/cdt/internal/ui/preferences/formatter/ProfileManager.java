/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.TextProcessor;

import org.eclipse.cdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.internal.ui.util.Messages;


/**
 * The model for the set of profiles which are available in the workbench.
 */
public abstract class ProfileManager extends Observable {
	
    public static final class KeySet {

		private final List fKeys;
		private final String fNodeName;

		public KeySet(String nodeName, List keys) {
			fNodeName= nodeName;
			fKeys= keys;
        }

        public String getNodeName() {
	        return fNodeName;
        }

        public List getKeys() {
	        return fKeys;
        }
    }
	
    /**
     * A prefix which is prepended to every ID of a user-defined profile, in order
     * to differentiate it from a built-in profile.
     */
	private final static String ID_PREFIX= "_"; //$NON-NLS-1$
	
	/**
	 * Represents a profile with a unique ID, a name and a map 
	 * containing the code formatter settings.
	 */
	public static abstract class Profile implements Comparable {
		
		public abstract String getName();
		public abstract Profile rename(String name, ProfileManager manager);
		
		public abstract Map getSettings();
		public abstract void setSettings(Map settings);
		
		public abstract int getVersion();
		
		public boolean hasEqualSettings(Map otherMap, List allKeys) {
			Map settings= getSettings();
			for (Iterator iter= allKeys.iterator(); iter.hasNext(); ){
				String key= (String) iter.next();
				Object other= otherMap.get(key);
				Object curr= settings.get(key);
				if (other == null) {
					if (curr != null) {
						return false;
					}
				} else if (!other.equals(curr)) {
					return false;
				}
			}
			return true;
		}
		
		public abstract boolean isProfileToSave();
		
		public abstract String getID();
		
		public boolean isSharedProfile() {
			return false;
		}
		
		public boolean isBuiltInProfile() {
			return false;
		}
	}
	
	/**
	 * Represents a built-in profile. The state of a built-in profile 
	 * cannot be changed after instantiation.
	 */
	public final static class BuiltInProfile extends Profile {
		private final String fName;
		private final String fID;
		private final Map fSettings;
		private final int fOrder;
		private final int fCurrentVersion;
		private final String fProfileKind;
		
		protected BuiltInProfile(String ID, String name, Map settings, int order, int currentVersion, String profileKind) {
			fName= TextProcessor.process(name);
			fID= ID;
			fSettings= settings;
			fOrder= order;
			fCurrentVersion= currentVersion;
			fProfileKind= profileKind;
		}
		
		public String getName() { 
			return fName;	
		}
		
		public Profile rename(String name, ProfileManager manager) {
			final String trimmed= name.trim();
		 	CustomProfile newProfile= new CustomProfile(trimmed, fSettings, fCurrentVersion, fProfileKind);
		 	manager.addProfile(newProfile);
			return newProfile;
		}
		
		public Map getSettings() {
			return fSettings;
		}
	
		public void setSettings(Map settings) {
		}
	
		public String getID() { 
			return fID; 
		}
		
		public final int compareTo(Object o) {
			if (o instanceof BuiltInProfile) {
				return fOrder - ((BuiltInProfile)o).fOrder;
			}
			return -1;
		}

		public boolean isProfileToSave() {
			return false;
		}
		
		public boolean isBuiltInProfile() {
			return true;
		}

        public int getVersion() {
	        return fCurrentVersion;
        }
	
	}

	/**
	 * Represents a user-defined profile. A custom profile can be modified after instantiation.
	 */
	public static class CustomProfile extends Profile {
		private String fName;
		private Map fSettings;
		protected ProfileManager fManager;
		private int fVersion;
		private final String fKind;

		public CustomProfile(String name, Map settings, int version, String kind) {
			fName= name;
			fSettings= settings;
			fVersion= version;
			fKind= kind;
		}
		
		public String getName() {
			return fName;
		}
		
		public Profile rename(String name, ProfileManager manager) {
			final String trimmed= name.trim();
			if (trimmed.equals(getName())) 
				return this;
			
			String oldID= getID(); // remember old id before changing name
			fName= trimmed;
			
			manager.profileRenamed(this, oldID);
			return this;
		}

		public Map getSettings() { 
			return fSettings;
		}
		
		public void setSettings(Map settings) {
			if (settings == null)
				throw new IllegalArgumentException();
			fSettings= settings;
			if (fManager != null) {
				fManager.profileChanged(this);
			}
		}
		
		public String getID() { 
			return ID_PREFIX + fName;
		}
		
		public void setManager(ProfileManager profileManager) {
			fManager= profileManager;
		}
		
		public ProfileManager getManager() {
			return fManager;
		}

		public int getVersion() {
			return fVersion;
		}
		
		public void setVersion(int version)	{
			fVersion= version;
		}
		
		public int compareTo(Object o) {
			if (o instanceof SharedProfile) {
				return -1;
			}
			if (o instanceof CustomProfile) {
				return getName().compareToIgnoreCase(((Profile)o).getName());
			}
			return 1;
		}
		
		public boolean isProfileToSave() {
			return true;
		}

        public String getKind() {
	        return fKind;
        }

	}
	
	public final static class SharedProfile extends CustomProfile {
		
		public SharedProfile(String oldName, Map options, int version, String profileKind) {
			super(oldName, options, version, profileKind);
		}
		
		public Profile rename(String name, ProfileManager manager) {
			CustomProfile profile= new CustomProfile(name.trim(), getSettings(), getVersion(), getKind());

			manager.profileReplaced(this, profile);
			return profile;
		}
				
		public String getID() { 
			return SHARED_PROFILE;
		}
		
		public final int compareTo(Object o) {
			return 1;
		}
		
		public boolean isProfileToSave() {
			return false;
		}
		
		public boolean isSharedProfile() {
			return true;
		}
	}
	

	/**
	 * The possible events for observers listening to this class.
	 */
	public final static int SELECTION_CHANGED_EVENT= 1;
	public final static int PROFILE_DELETED_EVENT= 2;
	public final static int PROFILE_RENAMED_EVENT= 3;
	public final static int PROFILE_CREATED_EVENT= 4;
	public final static int SETTINGS_CHANGED_EVENT= 5;

	
	/**
	 * The key of the preference where the selected profile is stored.
	 */
	private final String fProfileKey;
	
	/**
	 * The key of the preference where the version of the current settings is stored
	 */
	private final String fProfileVersionKey;

	public final static String SHARED_PROFILE= "org.eclipse.cdt.ui.default.shared"; //$NON-NLS-1$
	
	/**
	 * A map containing the available profiles, using the IDs as keys.
	 */
	private final Map fProfiles;
	
	/**
	 * The available profiles, sorted by name.
	 */
	private final List fProfilesByName;
	
	/**
	 * The currently selected profile. 
	 */
	private Profile fSelected;
	
	/**
	 * The keys of the options to be saved with each profile
	 */
	private final KeySet[] fKeySets;

	private final PreferencesAccess fPreferencesAccess;
	private final IProfileVersioner fProfileVersioner;
	
	/**
	 * Create and initialize a new profile manager.
	 * @param profiles Initial custom profiles (List of type <code>CustomProfile</code>)
	 * @param profileVersioner 
	 */
	public ProfileManager(
			List profiles, 
			IScopeContext context, 
			PreferencesAccess preferencesAccess, 
			IProfileVersioner profileVersioner,
			KeySet[] keySets,
			String profileKey,
			String profileVersionKey) {
		
		fPreferencesAccess= preferencesAccess;
		fProfileVersioner= profileVersioner;
		fKeySets= keySets;
		fProfileKey= profileKey;
		fProfileVersionKey= profileVersionKey;
		
		fProfiles= new HashMap();
		fProfilesByName= new ArrayList();
	
		for (final Iterator iter = profiles.iterator(); iter.hasNext();) {
			final Profile profile= (Profile) iter.next();
			if (profile instanceof CustomProfile) {
				((CustomProfile)profile).setManager(this);
			}
			fProfiles.put(profile.getID(), profile);
			fProfilesByName.add(profile);
		}

		Collections.sort(fProfilesByName);
		
		String profileId= getSelectedProfileId(fPreferencesAccess.getInstanceScope());
		
		Profile profile= (Profile) fProfiles.get(profileId);
		if (profile == null) {
			profile= getDefaultProfile();
		}
		fSelected= profile;
		
		if (context.getName() == ProjectScope.SCOPE && hasProjectSpecificSettings(context)) {
			Map map= readFromPreferenceStore(context, profile);
			if (map != null) {
				
				List allKeys= new ArrayList();
				for (int i= 0; i < fKeySets.length; i++) {
			        allKeys.addAll(fKeySets[i].getKeys());
		        }
		        Collections.sort(allKeys);
				
				Profile matching= null;
			
				String projProfileId= context.getNode(CUIPlugin.PLUGIN_ID).get(fProfileKey, null);
				if (projProfileId != null) {
					Profile curr= (Profile) fProfiles.get(projProfileId);
					if (curr != null && (curr.isBuiltInProfile() || curr.hasEqualSettings(map, allKeys))) {
						matching= curr;
					}
				} else {
					// old version: look for similar
					for (final Iterator iter = fProfilesByName.iterator(); iter.hasNext();) {
						Profile curr= (Profile) iter.next();
						if (curr.hasEqualSettings(map, allKeys)) {
							matching= curr;
							break;
						}
					}
				}
				if (matching == null) {
					String name;
					if (projProfileId != null && !fProfiles.containsKey(projProfileId)) {
						name= Messages.format(FormatterMessages.ProfileManager_unmanaged_profile_with_name, projProfileId.substring(ID_PREFIX.length()));
					} else {
						name= FormatterMessages.ProfileManager_unmanaged_profile;
					}
					// current settings do not correspond to any profile -> create a 'team' profile
					SharedProfile shared= new SharedProfile(name, map, fProfileVersioner.getCurrentVersion(), fProfileVersioner.getProfileKind());
					shared.setManager(this);
					fProfiles.put(shared.getID(), shared);
					fProfilesByName.add(shared); // add last
					matching= shared;
				}
				fSelected= matching;
			}
		}
	}
	
	protected String getSelectedProfileId(IScopeContext instanceScope) {
		String profileId= instanceScope.getNode(CUIPlugin.PLUGIN_ID).get(fProfileKey, null);
		if (profileId == null) {
			// request from bug 129427
			profileId= new DefaultScope().getNode(CUIPlugin.PLUGIN_ID).get(fProfileKey, null);
		}
	    return profileId;
    }

	/**
	 * Notify observers with a message. The message must be one of the following:
	 * @param message Message to send out
	 * 
	 * @see #SELECTION_CHANGED_EVENT
	 * @see #PROFILE_DELETED_EVENT
	 * @see #PROFILE_RENAMED_EVENT
	 * @see #PROFILE_CREATED_EVENT
	 * @see #SETTINGS_CHANGED_EVENT
	 */
	protected void notifyObservers(int message) {
		setChanged();
		notifyObservers(new Integer(message));
	}
	
	public static boolean hasProjectSpecificSettings(IScopeContext context, KeySet[] keySets) {
		for (int i= 0; i < keySets.length; i++) {
	        KeySet keySet= keySets[i];
	        IEclipsePreferences preferences= context.getNode(keySet.getNodeName());
	        for (final Iterator keyIter= keySet.getKeys().iterator(); keyIter.hasNext();) {
	            final String key= (String)keyIter.next();
	            Object val= preferences.get(key, null);
	            if (val != null) {
	            	return true;
	            }
            }
        }
		return false;
	}
	
	public boolean hasProjectSpecificSettings(IScopeContext context) {
		return hasProjectSpecificSettings(context, fKeySets);
	}

	/**
	 * Only to read project specific settings to find out to what profile it matches.
	 * @param context The project context
	 */
	public Map readFromPreferenceStore(IScopeContext context, Profile workspaceProfile) {
		final Map profileOptions= new HashMap();
		IEclipsePreferences uiPrefs= context.getNode(CUIPlugin.PLUGIN_ID);
				
		int version= uiPrefs.getInt(fProfileVersionKey, fProfileVersioner.getFirstVersion());
		if (version != fProfileVersioner.getCurrentVersion()) {
			Map allOptions= new HashMap();
			for (int i= 0; i < fKeySets.length; i++) {
	            addAll(context.getNode(fKeySets[i].getNodeName()), allOptions);
            }
			CustomProfile profile= new CustomProfile("tmp", allOptions, version, fProfileVersioner.getProfileKind()); //$NON-NLS-1$
			fProfileVersioner.update(profile);
			return profile.getSettings();
		}
		
		boolean hasValues= false;
		for (int i= 0; i < fKeySets.length; i++) {
	        KeySet keySet= fKeySets[i];
	        IEclipsePreferences preferences= context.getNode(keySet.getNodeName());
	        for (final Iterator keyIter = keySet.getKeys().iterator(); keyIter.hasNext(); ) {
				final String key= (String) keyIter.next();
				Object val= preferences.get(key, null);
				if (val != null) {
					hasValues= true;
				} else {
					val= workspaceProfile.getSettings().get(key);
				}
				profileOptions.put(key, val);
			}
        }
		
		if (!hasValues) {
			return null;
		}

		return profileOptions;
	}
	
	/**
	 * @param uiPrefs
	 * @param allOptions
	 */
	private void addAll(IEclipsePreferences uiPrefs, Map allOptions) {
		try {
			String[] keys= uiPrefs.keys();
			for (int i= 0; i < keys.length; i++) {
				String key= keys[i];
				String val= uiPrefs.get(key, null);
				if (val != null) {
					allOptions.put(key, val);
				}
			}
		} catch (BackingStoreException e) {
			// ignore
		}
		
	}

	private boolean updatePreferences(IEclipsePreferences prefs, List keys, Map profileOptions) {
		boolean hasChanges= false;
		for (final Iterator keyIter = keys.iterator(); keyIter.hasNext(); ) {
			final String key= (String) keyIter.next();
			final String oldVal= prefs.get(key, null);
			final String val= (String) profileOptions.get(key);
			if (val == null) {
				if (oldVal != null) {
					prefs.remove(key);
					hasChanges= true;
				}
			} else if (!val.equals(oldVal)) {
				prefs.put(key, val);
				hasChanges= true;
			}
		}
		return hasChanges;
	}
	
	
	/**
	 * Update all formatter settings with the settings of the specified profile. 
	 * @param profile The profile to write to the preference store
	 */
	private void writeToPreferenceStore(Profile profile, IScopeContext context) {
		final Map profileOptions= profile.getSettings();
		
		for (int i= 0; i < fKeySets.length; i++) {
	        updatePreferences(context.getNode(fKeySets[i].getNodeName()), fKeySets[i].getKeys(), profileOptions);
        }
		
		final IEclipsePreferences uiPrefs= context.getNode(CUIPlugin.PLUGIN_ID);
		if (uiPrefs.getInt(fProfileVersionKey, 0) != fProfileVersioner.getCurrentVersion()) {
			uiPrefs.putInt(fProfileVersionKey, fProfileVersioner.getCurrentVersion());
		}
		
		if (context.getName() == InstanceScope.SCOPE) {
			uiPrefs.put(fProfileKey, profile.getID());
		} else if (context.getName() == ProjectScope.SCOPE && !profile.isSharedProfile()) {
			uiPrefs.put(fProfileKey, profile.getID());
		}
	}
	
	/** 
	 * Get an immutable list as view on all profiles, sorted alphabetically. Unless the set 
	 * of profiles has been modified between the two calls, the sequence is guaranteed to 
	 * correspond to the one returned by <code>getSortedNames</code>.
	 * @return a list of elements of type <code>Profile</code>
	 * 
	 * @see #getSortedDisplayNames()
	 */
	public List getSortedProfiles() {
		return Collections.unmodifiableList(fProfilesByName);
	}

	/**
	 * Get the names of all profiles stored in this profile manager, sorted alphabetically. Unless the set of 
	 * profiles has been modified between the two calls, the sequence is guaranteed to correspond to the one 
	 * returned by <code>getSortedProfiles</code>.
	 * @return All names, sorted alphabetically
	 * @see #getSortedProfiles()  
	 */	
	public String[] getSortedDisplayNames() {
		final String[] sortedNames= new String[fProfilesByName.size()];
		int i= 0;
		for (final Iterator iter = fProfilesByName.iterator(); iter.hasNext();) {
			Profile curr= (Profile) iter.next();
			sortedNames[i++]= curr.getName();
		}
		return sortedNames;
	}
	
	/**
	 * Get the profile for this profile id.
	 * @param ID The profile ID
	 * @return The profile with the given ID or <code>null</code> 
	 */
	public Profile getProfile(String ID) {
		return (Profile)fProfiles.get(ID);
	}
	
	/**
	 * Activate the selected profile, update all necessary options in
	 * preferences and save profiles to disk.
	 */
	public void commitChanges(IScopeContext scopeContext) {
		if (fSelected != null) {
			writeToPreferenceStore(fSelected, scopeContext);
		}
	}
	
	public void clearAllSettings(IScopeContext context) {
		for (int i= 0; i < fKeySets.length; i++) {
	        updatePreferences(context.getNode(fKeySets[i].getNodeName()), fKeySets[i].getKeys(), Collections.EMPTY_MAP);
        }
		
		final IEclipsePreferences uiPrefs= context.getNode(CUIPlugin.PLUGIN_ID);
		uiPrefs.remove(fProfileKey);
	}
	
	/**
	 * Get the currently selected profile.
	 * @return The currently selected profile.
	 */
	public Profile getSelected() {
		return fSelected;
	}

	/**
	 * Set the selected profile. The profile must already be contained in this profile manager.
	 * @param profile The profile to select
	 */
	public void setSelected(Profile profile) {
		final Profile newSelected= (Profile)fProfiles.get(profile.getID());
		if (newSelected != null && !newSelected.equals(fSelected)) {
			fSelected= newSelected;
			notifyObservers(SELECTION_CHANGED_EVENT);
		}
	}

	/**
	 * Check whether a user-defined profile in this profile manager
	 * already has this name.
	 * @param name The name to test for
	 * @return Returns <code>true</code> if a profile with the given name exists
	 */
	public boolean containsName(String name) {
		for (final Iterator iter = fProfilesByName.iterator(); iter.hasNext();) {
			Profile curr= (Profile) iter.next();
			if (name.equals(curr.getName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add a new custom profile to this profile manager.
	 * @param profile The profile to add
	 */	
	public void addProfile(CustomProfile profile) {
		profile.setManager(this);
		final CustomProfile oldProfile= (CustomProfile)fProfiles.get(profile.getID());
		if (oldProfile != null) {
			fProfiles.remove(oldProfile.getID());
			fProfilesByName.remove(oldProfile);
			oldProfile.setManager(null);
		}
		fProfiles.put(profile.getID(), profile);
		fProfilesByName.add(profile);
		Collections.sort(fProfilesByName);
		fSelected= profile;
		notifyObservers(PROFILE_CREATED_EVENT);
	}
	
	/**
	 * Delete the currently selected profile from this profile manager. The next profile
	 * in the list is selected.
	 * @return true if the profile has been successfully removed, false otherwise.
	 */
	public boolean deleteSelected() {
		if (!(fSelected instanceof CustomProfile)) 
			return false;
		
		return deleteProfile((CustomProfile)fSelected);
	}

	public boolean deleteProfile(CustomProfile profile) {
	    int index= fProfilesByName.indexOf(profile);
		
		fProfiles.remove(profile.getID());
		fProfilesByName.remove(profile);
		
		profile.setManager(null);
		
		if (index >= fProfilesByName.size())
			index--;
		fSelected= (Profile) fProfilesByName.get(index);

		if (!profile.isSharedProfile()) {
			updateProfilesWithName(profile.getID(), null, false);
		}
		
		notifyObservers(PROFILE_DELETED_EVENT);
		return true;
    }
	
	public void profileRenamed(CustomProfile profile, String oldID) {
		fProfiles.remove(oldID);
		fProfiles.put(profile.getID(), profile);

		if (!profile.isSharedProfile()) {
			updateProfilesWithName(oldID, profile, false);
		}
		
		Collections.sort(fProfilesByName);
		notifyObservers(PROFILE_RENAMED_EVENT);
	}
	
	public void profileReplaced(CustomProfile oldProfile, CustomProfile newProfile) {
		fProfiles.remove(oldProfile.getID());
		fProfiles.put(newProfile.getID(), newProfile);
		fProfilesByName.remove(oldProfile);
		fProfilesByName.add(newProfile);
		Collections.sort(fProfilesByName);
		
		if (!oldProfile.isSharedProfile()) {
			updateProfilesWithName(oldProfile.getID(), null, false);
		}
		
		setSelected(newProfile);
		notifyObservers(PROFILE_CREATED_EVENT);
		notifyObservers(SELECTION_CHANGED_EVENT);
	}
	
	public void profileChanged(CustomProfile profile) {
		if (!profile.isSharedProfile()) {
			updateProfilesWithName(profile.getID(), profile, true);
		}
		
		notifyObservers(SETTINGS_CHANGED_EVENT);
	}
	
	
	private void updateProfilesWithName(String oldName, Profile newProfile, boolean applySettings) {
		IProject[] projects= ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i= 0; i < projects.length; i++) {
			IScopeContext projectScope= fPreferencesAccess.getProjectScope(projects[i]);
			IEclipsePreferences node= projectScope.getNode(CUIPlugin.PLUGIN_ID);
			String profileId= node.get(fProfileKey, null);
			if (oldName.equals(profileId)) {
				if (newProfile == null) {
					node.remove(fProfileKey);
				} else {
					if (applySettings) {
						writeToPreferenceStore(newProfile, projectScope);
					} else {
						node.put(fProfileKey, newProfile.getID());
					}
				}
			}
		}
		
		IScopeContext instanceScope= fPreferencesAccess.getInstanceScope();
		final IEclipsePreferences uiPrefs= instanceScope.getNode(CUIPlugin.PLUGIN_ID);
		if (newProfile != null && oldName.equals(uiPrefs.get(fProfileKey, null))) {
			writeToPreferenceStore(newProfile, instanceScope);
		}
	}

    public abstract Profile getDefaultProfile();

	public IProfileVersioner getProfileVersioner() {
    	return fProfileVersioner;
    }
}
