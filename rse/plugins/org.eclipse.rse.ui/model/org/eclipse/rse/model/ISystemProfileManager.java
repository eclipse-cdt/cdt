/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.model;
import java.util.Vector;

import org.eclipse.rse.ui.validators.ISystemValidator;


/**
 * A class that manages a list of SystemProfile objects.
 */
/**
 * @lastgen interface SystemProfileManager  {}
 */

public interface ISystemProfileManager {

	
	/**
	 * Create a new profile with the given name, and add to the list.
	 * The name must be unique within the existing list.
	 * <p>
	 * The underlying folder is created in the file system.
	 * <p>
	 * @param name What to name this profile
	 * @param makeActive true if this profile is to be added to the active profile list.
	 * @return new profile, or null if name not unique.
	 */
	public ISystemProfile createSystemProfile(String name, boolean makeActive);
	/**
	 * Toggle an existing profile's state between active and inactive
	 */
	public void makeSystemProfileActive(ISystemProfile profile, boolean makeActive);
	/**
	 * Get an array of all existing profiles.
	 */
	public ISystemProfile[] getSystemProfiles();
	/**
	 * Get an array of all existing profile names.
	 */
	public String[] getSystemProfileNames();	
	/**
	 * Get a vector of all existing profile names.
	 */
	public Vector getSystemProfileNamesVector();	
	/**
	 * Get a profile given its name.
	 */
	public ISystemProfile getSystemProfile(String name);
	/**
	 * Return the profiles identified via preferences as the active profiles...
	 */
	public ISystemProfile[] getActiveSystemProfiles();
	/**
	 * Return the profile names currently selected by the user as his "active" profiles
	 */
	public String[] getActiveSystemProfileNames();
	/**
	 * Return 0-based position of the given active profile within the list of active profiles.
	 */
	public int getActiveSystemProfilePosition(String profileName);
	/**
	 * Return the default private profile created at first touch.
	 * Will return null if it has been renamed!
	 */
	public ISystemProfile getDefaultPrivateSystemProfile();
	/**
	 * Return the default team profile created at first touch.
	 * Will return null if it has been renamed!
	 */
	public ISystemProfile getDefaultTeamSystemProfile();
	/**
	 * Rename the given profile.
	 */
	public void renameSystemProfile(ISystemProfile profile, String newName);
	/**
	 * Delete the given profile
	 */
	public void deleteSystemProfile(ISystemProfile profile);
	/**
	 * Clone the given profile
	 */
	public ISystemProfile cloneSystemProfile(ISystemProfile profile, String newName);
    /**
     * Return true if the given profile is active
     * @see ISystemProfile#isActive()
     */
    public boolean isSystemProfileActive(String profileName);

	/**
	 * Reusable method to return a name validator for renaming a profile.
	 * @param the current profile name on updates. Can be null for new profiles. Used
	 *  to remove from the existing name list the current connection.
	 */
	public ISystemValidator getProfileNameValidator(String profileName);
	/**
	 * Reusable method to return a name validator for renaming a profile.
	 * @param the current profile object on updates. Can be null for new profiles. Used
	 *  to remove from the existing name list the current connection.
	 */
	public ISystemValidator getProfileNameValidator(ISystemProfile profile);

	
	

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of Profiles references
	 */
	java.util.List getProfiles();

}