/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.internal.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorProfileName;

//
//

/**
 * A class that manages a list of SystemProfile objects.
 * We use this as a singleton.
 */
/**
 * @lastgen class SystemProfileManagerImpl Impl implements SystemProfileManager, EObject {}
 */

public class SystemProfileManager implements ISystemProfileManager
{
	private List               _profiles = null;
	private String[]            profileNames = null;
	private Vector              profileNamesVector = null;
	private static              ISystemProfileManager defaultInst = null;
	private static final String PROFILE_FILE_NAME = "profile";
	
/**
	 * Default constructor
	 */
	protected SystemProfileManager() 
	{
		super();
	}
	/**
	 * Return (and create if necessary) the singleton instance of this class.
	 */
	public static ISystemProfileManager getSystemProfileManager()
	{
		if (defaultInst == null)
		{

			defaultInst = new SystemProfileManager();
			
			// restores all of RSE
			SystemPlugin.getThePersistenceManager().restore(defaultInst);
		}
		return defaultInst;
	}
	
	/**
	 * Clear the default after a team sychronization say
	 */
	public static void clearDefault()
	{
		defaultInst = null;
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
	 */
	public ISystemProfile createSystemProfile(String name, boolean makeActive)
	{
		// FIXME 
		ISystemProfile existingProfile = getSystemProfile(name);
		if (existingProfile != null)
		{
			// replace the existing one with a new profile
			deleteSystemProfile(existingProfile);
		}
		
		ISystemProfile newProfile = internalCreateSystemProfileAndFolder(name);
		if (makeActive)
		{
		  	SystemPreferencesManager.getPreferencesManager().addActiveProfile(name);
		  	((SystemProfile)newProfile).setActive(makeActive);
		}	
		SystemPlugin.getThePersistenceManager().commit(this);
		return newProfile;
	}
	
	/**
	 * Toggle an existing profile's state between active and inactive
	 */
	public void makeSystemProfileActive(ISystemProfile profile, boolean makeActive)
	{
        boolean wasActive = isSystemProfileActive(profile.getName());
		if (wasActive && !makeActive)
		  	SystemPreferencesManager.getPreferencesManager().deleteActiveProfile(profile.getName());
		else if (makeActive && !wasActive)
		  	SystemPreferencesManager.getPreferencesManager().addActiveProfile(profile.getName());		
		((SystemProfile)profile).setActive(makeActive);
	}
	
	/*
	 * private version that avoids name collision check
	 */
	private ISystemProfile internalCreateSystemProfile(String name)
	{
        ISystemProfile profile = new SystemProfile();
        
        	// FIXME initMOF().createSystemProfile();                
        initialize(profile, name);
		profile.setDefaultPrivate(name.equalsIgnoreCase("Private"));
		//System.out.println("initializing new profile " + name + ", is default private? " + profile.isDefaultPrivate());
        return profile;
	}

	/*
	 * private version that avoids name collision check
	 */
	private ISystemProfile internalCreateSystemProfileAndFolder(String name)
	{
        ISystemProfile profile = internalCreateSystemProfile(name);        
        SystemResourceManager.getProfileFolder(profile); // creates proj/profileName folder
         return profile;
	}
	
	/*
	 * private method to initialize state for new profile
	 */
	private void initialize(ISystemProfile profile, String name)
	{
		profile.setName(name);
		profile.setProfileManager(this);
		getProfiles().add(profile);
        invalidateCache();
	}
	
	/**
	 * Get an array of all existing profiles.
	 */
	public ISystemProfile[] getSystemProfiles()
	{
		List profiles = getProfiles();

		
		// Ensure that one Profile is the default Profile - defect 48995 NH	
		boolean defaultProfileExist = false;
		for (int idx=0; (!defaultProfileExist) && (idx<profiles.size()); idx++)
		{ 
			ISystemProfile profile = (ISystemProfile)profiles.get(idx);
			if (profile.isDefaultPrivate())
			{
				defaultProfileExist = true;	
			}				    
		}      	      
		if (!defaultProfileExist) 
		{ 
		  // Check if the Profile exists with name same as the LocalMachine Name - this is the default we give
		  // when creating connections. 	
		  for (int idx=0; (!defaultProfileExist) && (idx<profiles.size()); idx++)
		  {
		  	ISystemProfile profile = (ISystemProfile)profiles.get(idx);
      		String initProfileName = SystemPlugin.getLocalMachineName();
      		int dotIndex = initProfileName.indexOf('.');
      		
      		if (dotIndex != -1)
      		{
      			initProfileName = initProfileName.substring(0, dotIndex);
      		}
      		
		  	if (profile.getName().equalsIgnoreCase(initProfileName))
		  	{
		  	   profile.setDefaultPrivate(true);
		       defaultProfileExist = true; 
		  	}	
		  }
		  
		  // If did not find such a profile then the first profile found besides Team is set to be the default profile 	
		  if (!defaultProfileExist)
		  {			  
		  	for (int idx=0; (!defaultProfileExist) && (idx<profiles.size()); idx++)
		  	{
		  		ISystemProfile profile = (ISystemProfile)profiles.get(idx);
		  	   if (!profile.getName().equalsIgnoreCase(ISystemPreferencesConstants.DEFAULT_TEAMPROFILE)) 
		       {
		         profile.setDefaultPrivate(true);
		
		         SystemPlugin.getThePersistenceManager().commit(SystemStartHere.getSystemProfileManager());
		         defaultProfileExist = true; 
		       }
		  	}   	
		  }
		  if (!defaultProfileExist)
		  {
		  	 // If Team is the only profile - then put a message in the log - do not make Team to be default
		     if (profiles.size() == 1 && ((ISystemProfile)profiles.get(0)).getName().equalsIgnoreCase("Team"))  
		     {
		  	    SystemBasePlugin.logWarning("Only one Profile Team exists - there is no Default Profile");
		     } 
		     else 
		     {  
		    	 // sonething must have gone wrong - it should not come here
		        SystemBasePlugin.logWarning("Something went wrong and the default profile is not set"); 
		     }
		  }	
		}  
	    return (ISystemProfile[])profiles.toArray(new ISystemProfile[profiles.size()]);
	}

	/**
	 * Get an array of all existing profile names.
	 */
	public String[] getSystemProfileNames()
	{
		if (profileNames == null)
		{
 		  ISystemProfile[] profiles = getSystemProfiles();
		  profileNames = new String[profiles.length];
		  for (int idx = 0; idx < profiles.length; idx++)
		   profileNames[idx] = profiles[idx].getName();
		}
	    return profileNames;
	}
	/**
	 * Get a vector of all existing profile names.
	 */
	public Vector getSystemProfileNamesVector()
	{
		if (profileNamesVector == null)
		{
		  ISystemProfile[] profiles = getSystemProfiles();
		  profileNamesVector = new Vector(profiles.length);
		  for (int idx = 0; idx < profiles.length; idx++)
		     profileNamesVector.addElement(profiles[idx].getName());
		}
	    return profileNamesVector;
	}

	
	/**
	 * Something changed so invalide cache of profiles so it will be regenerated
	 */
	protected void invalidateCache()
	{
//DY	profiles = null;
		profileNames = null;
		profileNamesVector = null;
	}
	
	/**
	 * Get a profile given its name.
	 */
	public ISystemProfile getSystemProfile(String name)
	{
	    ISystemProfile[] profiles = getSystemProfiles();
	    if ((profiles == null) || (profiles.length==0))
	      return null;
	    ISystemProfile match = null;
	    for (int idx=0; (match==null) && (idx<profiles.length); idx++)
	       if (profiles[idx].getName().equals(name))
	         match = profiles[idx];
	    return match;             	
	}
	
	/**
	 * Rename the given profile.
	 * This will:
	 * <ul>
	 *    <li>Rename the profile in memory
	 *    <li>Rename the underlying folder
	 *    <li>Update the user preferences if this profile is currently active.
	 * </ul>
	 */
	public void renameSystemProfile(ISystemProfile profile, String newName)
	{
		boolean isActive = isSystemProfileActive(profile.getName());
		String oldName = profile.getName();
		profile.setName(newName);
		if (isActive)
		  SystemPreferencesManager.getPreferencesManager().renameActiveProfile(oldName, newName);
		invalidateCache(); 
		// FIXME SystemPlugin.getThePersistanceManager().save(this);
	}
	
	/**
	 * Delete the given profile
	 * This will:
	 * <ul>
	 *    <li>Delete the profile in memory
	 *    <li>Delete the underlying folder
	 *    <li>Update the user preferences if this profile is currently active.
	 * </ul>
	 */
	public void deleteSystemProfile(ISystemProfile profile)
	{
		String oldName = profile.getName();
		boolean isActive = isSystemProfileActive(oldName);
		
        getProfiles().remove(profile);
        /* FIXME
		// now in EMF, the profiles are "owned" by the Resource, and only referenced by the profile manager,
		//  so I don't think just removing it from the manager is enough... it must also be removed from its
		//  resource. Phil.
        Resource res = profile.eResource();
        if (res != null)
          res.getContents().remove(profile);      
*/
		if (isActive)
		  SystemPreferencesManager.getPreferencesManager().deleteActiveProfile(oldName);
        invalidateCache();	
        
        //FIXME SystemPlugin.getThePersistanceManager().save(this);
   
	}

	/**
	 * Clone the given profile to a new one with the given name.
	 * Pretty useless right now, as there is no data to clone!
	 */
	public ISystemProfile cloneSystemProfile(ISystemProfile profile, String newName)
	{
		ISystemProfile newProfile = createSystemProfile(newName, false);
		return newProfile;
	}

    /**
     * Return true if the given profile is active.
     * @see ISystemProfile#isActive()
     */
    public boolean isSystemProfileActive(String profileName)
    {
    	String[] activeProfiles = getActiveSystemProfileNames();
    	boolean match = false;
    	for (int idx=0; !match && (idx<activeProfiles.length); idx++)
    	{
    		if (activeProfiles[idx].equals(profileName))
    		  match = true;
    	}
    	return match;
    }
    // ---------------------------
    // RETURN SPECIFIC PROFILES...
    // ---------------------------
	/**
	 * Return the profiles currently selected by the user as his "active" profiles
	 */
	public ISystemProfile[] getActiveSystemProfiles()
	{
		String[] profileNames = getActiveSystemProfileNames();
		ISystemProfile[] profiles = new ISystemProfile[profileNames.length];
		for (int idx=0; idx<profileNames.length; idx++)
		{
		   profiles[idx] = getOrCreateSystemProfile(profileNames[idx]);
		   ((SystemProfile)profiles[idx]).setActive(true);
		}
		return profiles;
	}
	/**
	 * Return the profile names currently selected by the user as his "active" profiles
	 */
	public String[] getActiveSystemProfileNames()
	{		
		String[] activeProfileNames = 
		  SystemPreferencesManager.getPreferencesManager().getActiveProfileNames();
		  
		// dy: defect 48355, need to sync this with the actual profile list.  If the user
		// imports old preference settings or does a team sync and a profile is deleted then
		// it is possible an active profile no longer exists.
		//String[] systemProfileNames = getSystemProfileNames();
		  ISystemProfile[] systemProfiles = getSystemProfiles();
		boolean found;
		boolean found_team = false;
		boolean found_private = false;
		boolean changed = false;

		for (int activeIdx = 0; activeIdx < activeProfileNames.length; activeIdx++)
		{
			// skip Team and Private profiles
			if (SystemPlugin.getLocalMachineName().equals(activeProfileNames[activeIdx]))
			{
				found_private = true;
			}
			else if (ISystemPreferencesConstants.DEFAULT_TEAMPROFILE.equals(activeProfileNames[activeIdx]))
			{
				found_team = true;
			}
			else
			{
				found = false;
				for (int systemIdx = 0; systemIdx < systemProfiles.length && !found; systemIdx++)
				{
					if (activeProfileNames[activeIdx].equals(systemProfiles[systemIdx].getName()))
					{
						found = true;
					}
				}
				
				if (!found)
				{
					// The active profile no longer exists so remove it from the active list
					SystemPreferencesManager.getPreferencesManager().deleteActiveProfile(activeProfileNames[activeIdx]);
					changed = true;
				}
			}
		}
		
		for (int systemIdx = 0; systemIdx < systemProfiles.length && !changed; systemIdx++)
		{
		    boolean matchesBoth = false;
		    String name = systemProfiles[systemIdx].getName();
	
		    for (int activeIdx = 0; activeIdx < activeProfileNames.length && !matchesBoth; activeIdx++)
			{
		        String aname = activeProfileNames[activeIdx];
		        if (name.equals(aname))
		        {		            
		            matchesBoth = true;
		        }
		     
			}
		    if (!matchesBoth && found_private)
		    {
		        if (systemProfiles[systemIdx].isActive() || systemProfiles[systemIdx].isDefaultPrivate())
		        {
		            SystemPreferencesManager.getPreferencesManager().addActiveProfile(name);
		            SystemPreferencesManager.getPreferencesManager().deleteActiveProfile(SystemPlugin.getLocalMachineName());
		            activeProfileNames = SystemPreferencesManager.getPreferencesManager().getActiveProfileNames();		            
		        }
		    }
		}
	
		
		// the active profiles list needed to be changed because of an external update, also
		// check if Default profile needs to be added back to the list
		if (changed || !found_team || !found_private)
		{
	      	if (systemProfiles.length == 0)
	      	{
	      		// First time user, make sure default is in the active list, the only time it wouldn't
	      		// be is if the pref_store.ini was modified (because the user imported old preferences)
				if (!found_team)
				{
					SystemPreferencesManager.getPreferencesManager().addActiveProfile(ISystemPreferencesConstants.DEFAULT_TEAMPROFILE);
					changed = true;
				}
				
				if (!found_private)
				{
					SystemPreferencesManager.getPreferencesManager().addActiveProfile(SystemPlugin.getLocalMachineName());
					changed = true;
				}				
	      	}
	      	else
	      	{
			   	ISystemProfile defaultProfile = getDefaultPrivateSystemProfile();
		      	if (defaultProfile != null && !found_private)
		      	{
		      		SystemPreferencesManager.getPreferencesManager().addActiveProfile(defaultProfile.getName());
		      		changed = true;
		      	}
	      	}

			if (changed)
			{			
				activeProfileNames = SystemPreferencesManager.getPreferencesManager().getActiveProfileNames();
			}
		}
				
		return activeProfileNames;
	}
	/**
	 * Return the profile names currently selected by the user as his "active" profiles
	 */
	public Vector getActiveSystemProfileNamesVector()
	{		
		String[] profileNames =
		  SystemPreferencesManager.getPreferencesManager().getActiveProfileNames();
		Vector v = new Vector(profileNames.length);
		for (int idx=0; idx<profileNames.length; idx++)
		  v.addElement(profileNames[idx]);
		return v;
	}
	/**
	 * Return 0-based position of the given active profile within the list of active profiles.
	 */
	public int getActiveSystemProfilePosition(String profileName)
	{
		String[] profiles = getActiveSystemProfileNames();
		int pos = -1;
		for (int idx=0; (pos<0) && (idx<profiles.length); idx++)
		{
			if (profiles[idx].equals(profileName))
			  pos = idx;
		}
		return pos;
	}
	/**
	 * Return the default private profile created at first touch.
	 * Will return null if it has been renamed!
	 */
	public ISystemProfile getDefaultPrivateSystemProfile()
	{
		return getSystemProfile(SystemPlugin.getLocalMachineName());
	}
	/**
	 * Return the default team profile created at first touch.
	 * Will return null if it has been renamed!
	 */
	public ISystemProfile getDefaultTeamSystemProfile()
	{
		return getSystemProfile(ISystemPreferencesConstants.DEFAULT_TEAMPROFILE);
	}


	/**
	 * Instantiate a user profile given its name.
	 */
	protected ISystemProfile getOrCreateSystemProfile(String userProfileName)
	{
		ISystemProfile userProfile = getSystemProfile(userProfileName);
		//System.out.println("in gorcSp for "+userProfileName+". userProfile==null? "+(userProfile==null));
		if (userProfile == null)
		{
			userProfile = internalCreateSystemProfileAndFolder(userProfileName);
		}
		return userProfile;
	}



	
   
    // ------------------
    // UTILITY METHODS...
    // ------------------ 
    
    /**
     * Return the unqualified save file name with the extension .xmi
     */
    public static String getSaveFileName(String profileName)
    {
        return null;//FIXME SystemMOFHelpers.getSaveFileName(getRootSaveFileName(profileName));
    }

    /**
     * Return the unqualified save file name with the extension .xmi
     */
    public static String getSaveFileName(ISystemProfile profile)
    {
        return null;//FIXME SystemMOFHelpers.getSaveFileName(getRootSaveFileName(profile));
    }

    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(ISystemProfile profile)
    {
        return getRootSaveFileName(profile.getName());
    }
    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(String profileName)
    {
    	//String fileName = profileName; // maybe a bad idea to include manager name in it!
    	String fileName = PROFILE_FILE_NAME;
        return fileName;    	
    }


    



	/**
	 * Reusable method to return a name validator for renaming a profile.
	 * @param the current profile name on updates. Can be null for new profiles. Used
	 *  to remove from the existing name list the current connection.
	 */
	public ISystemValidator getProfileNameValidator(String profileName)
	{
    	//Vector v = getActiveSystemProfileNamesVector();
    	Vector v = getSystemProfileNamesVector();    	
    	if (profileName != null)
    	  v.removeElement(profileName);
	    ISystemValidator nameValidator = new ValidatorProfileName(v);		
	    return nameValidator;
	}	
	/**
	 * Reusable method to return a name validator for renaming a profile.
	 * @param the current profile object on updates. Can be null for new profiles. Used
	 *  to remove from the existing name list the current connection.
	 */
	public ISystemValidator getProfileNameValidator(ISystemProfile profile)
	{
		if (profile != null)
		  return getProfileNameValidator(profile.getName());
		else
		  return getProfileNameValidator((String)null);		  
	}	
    
	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public java.util.List getProfiles()
	{
		if (_profiles == null)
		{
			// FIXME
			ISystemProfile profile = new SystemProfile();
			//profile.setName("Private");
			profile.setName(SystemPlugin.getLocalMachineName());
			profile.setDefaultPrivate(true);
			_profiles = new ArrayList();
			_profiles.add(profile);
			//profiles = null;//FIXME new EObjectResolvingeList(SystemProfile.class, this, ModelPackage.SYSTEM_PROFILE_MANAGER__PROFILES);
		}
		return _profiles;
	}

}