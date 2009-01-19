/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - [197036] added commitSystemProfile operation to interface
 * David Dykstal (IBM) - [202630] getDefaultPrivateProfile() and ensureDefaultPrivateProfile() are inconsistent
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.core.runtime.IStatus;

/**
 * Manages a list of SystemProfile objects. System profiles should be created,
 * deleted, restored, activated, and deactivated though this interface if event
 * processing is not desired. If events are necessary then the system registry
 * should be used.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. The
 *           standard implementations are included in the framework.
 */
public interface ISystemProfileManager {

	/**
	 * Create a new profile with the given name, and add to the list.
	 * The name must be unique within the existing list.
	 * <p>
	 * @param name What to name this profile
	 * @param makeActive true if this profile is to be added to the active profile list.
	 * @return new profile, or null if name not unique.
	 */
	public ISystemProfile createSystemProfile(String name, boolean makeActive);

	/**
	 * Toggle an existing profile's state between active and inactive.
	 * The default private profile cannot be deactivated and such a request will be ignored.
	 * @param profile the profile to (in)activate
	 * @param makeActive the state to make this profile
	 */
	public void makeSystemProfileActive(ISystemProfile profile, boolean makeActive);

	/**
	 * @return an array of all existing profiles. This is guaranteed to contain the
	 * default private profile.
	 */
	public ISystemProfile[] getSystemProfiles();

	/**
	 * @return the number of profiles known to this manager.
	 */
	public int getSize();

	/**
	 * @return an array of all existing profile names.
	 */
	public String[] getSystemProfileNames();

	/**
	 * Get a profile given its name.
	 * @param name the name of the profile
	 * @return the profile
	 */
	public ISystemProfile getSystemProfile(String name);

	/**
	 * @return the profiles identified via preferences as the active profiles...
	 */
	public ISystemProfile[] getActiveSystemProfiles();

	/**
	 * @return the profile names currently selected by the user as his "active" profiles
	 */
	public String[] getActiveSystemProfileNames();

	/**
	 * @return the default private profile created at first touch.
	 * Will return null if it has been renamed.
	 */
	public ISystemProfile getDefaultPrivateSystemProfile();

	/**
	 * @return the default team profile created at first touch.
	 * Will return null if it has been renamed.
	 */
	public ISystemProfile getDefaultTeamSystemProfile();

	/**
	 * Rename the given profile.
	 * @param profile the profile to rename
	 * @param newName the new profile name
	 */
	public void renameSystemProfile(ISystemProfile profile, String newName);

	/**
	 * Delete the given profile. The default private profile cannot be deleted and such a request will be ignored.
	 * @param profile the name of the profile to delete.
	 * @param persist true if the deletion is meant to be persisted as well, false if the deletion is just in the
	 * model.
	 */
	public void deleteSystemProfile(ISystemProfile profile, boolean persist);

	/**
	 * Clone the given profile
	 * @param profile the profile to clone
	 * @param newName the name of the new profile
	 * @return the new profile
	 */
	public ISystemProfile cloneSystemProfile(ISystemProfile profile, String newName);

	/**
	 * Commit a system profile
	 *
	 * @param profile the profile to commit
	 * @return a status object indicating the result of the commit
	 * @since org.eclipse.rse.core 3.0
	 */
	public IStatus commitSystemProfile(ISystemProfile profile);

	/**
	 * Get an indication of whether a profile is active or not.
	 * @param profileName the name of the profile to test
	 * @return true if the given profile is active
	 * @see ISystemProfile#isActive()
	 */
	public boolean isSystemProfileActive(String profileName);

	/**
	 * Adds a system profile to this profile manager.
	 * @param profile The system profile to add.
	 */
	public void addSystemProfile(ISystemProfile profile);

}
