/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.propertypages.RemoteSystemsPreferencePage;


/**
 * A class that encapsulates all global preferences for the remote system framework
 * <p>
 * These include:
 * <ul>
 *   <li>The list of profile names that are active
 *   <li>The default user Id per system type
 *   <li>The global setting about whether to show filter pools
 *   <li>The global setting about whether to show filter strings
 *   <li>
 * </ul>
 */
public class SystemPreferencesManager 
             implements ISystemPreferencesConstants
{
	
    private static SystemPreferencesManager inst;
    private Hashtable userIdsPerKey = null;
    /**
     * 
     */
    protected SystemPreferencesManager()
    {
    }
    
    /**
     * Get singleton of this class
     */
    public static SystemPreferencesManager getPreferencesManager()
    {
    	if (inst == null)
    	  inst = new SystemPreferencesManager();
    	return inst;
    }
    
    // -------------------------
    // ACTIVE PROFILE METHODS...
    // -------------------------
    
    /**
     * Return active profile names
     */
    public String[] getActiveProfileNames()
    {
    	return RemoteSystemsPreferencePage.getActiveProfiles();
    }
    
    /**
     * Rename one of the active profile names in the list of names stored in the registry.
     */
    public void renameActiveProfile(String oldName, String newName)
    {
    	// update active profile name list
		String[] names = getActiveProfileNames();
		int matchPos = -1;
		for (int idx=0; (matchPos==-1) && (idx<names.length); idx++)
		{
           if (names[idx].equalsIgnoreCase(oldName))		   
           {
             matchPos = idx;
             names[idx] = newName;
           }
		}
		if (matchPos >= 0)
		  RemoteSystemsPreferencePage.setActiveProfiles(names);    
    }
    /**
     * Delete one of the active profile names in the list of names stored in the registry.
     */
    public void deleteActiveProfile(String oldName)
    {
		String[] names = getActiveProfileNames();
		int matchPos = -1;
		for (int idx=0; (matchPos==-1) && (idx<names.length); idx++)
		{
           if (names[idx].equalsIgnoreCase(oldName))		   
           {
             matchPos = idx;
             names[idx] = null;
           }
		}
		if (matchPos >= 0)
		  RemoteSystemsPreferencePage.setActiveProfiles(names);    	
    }
    /**
     * Add a name to the active profile list. A name already in the list is not added again.
     * The list remains sorted in the natural order.
     */
    public void addActiveProfile(String newName)
    {
		SortedSet names = new TreeSet(Arrays.asList(getActiveProfileNames()));
		names.add(newName);
		String[] newNames = new String[names.size()];
		names.toArray(newNames);
		RemoteSystemsPreferencePage.setActiveProfiles(newNames);    	        
    }

    /**
     * Return the position of a give profile name in the active list
     */
	public int getActiveProfilePosition(String profileName)
	{
		String[] names = getActiveProfileNames();
		int matchPos = -1;
		for (int idx=0; (matchPos==-1) && (idx<names.length); idx++)
		{
           if (names[idx].equalsIgnoreCase(profileName))		   
             matchPos = idx;
		}
		return matchPos;
	}

   // ------------------
   // ORDER METHODS...
   // ------------------
   /**
    * Returns user's preference for the order of the connection names
    */
   public String[] getConnectionNamesOrder(String profileName)
   {
   	   String[] allConnectionNamesOrder = RemoteSystemsPreferencePage.getConnectionNamesOrder();
   	   profileName = profileName + ".";
   	   int profileNameLength = profileName.length();
   	   Vector v = new Vector();
   	   for (int idx=0; idx<allConnectionNamesOrder.length; idx++)
   	      if (allConnectionNamesOrder[idx].startsWith(profileName))
   	        v.addElement(allConnectionNamesOrder[idx].substring(profileNameLength));
   	   String[] names = new String[v.size()];
   	   //System.out.println("Connection names preference order for profile " + profileName + "...");   	   
   	   for (int idx=0; idx<names.length; idx++)
   	   {
   	      names[idx] = (String)v.elementAt(idx);   	   
   	      //System.out.println("   '" + names[idx]+"'");
   	   }
   	   //System.out.println();
   	   return names;
   }
   /**
    * Returns user's preference for the order of the connection names,
    *  after resolving it with the reality list of connection names.
    */
   public String[] getConnectionNamesOrder(IHost[] realityConnectionList, String profileName)
   {
   	   if (realityConnectionList == null)
   	     return new String[0];
   	   String[] realityNames = new String[realityConnectionList.length];
   	   for (int idx=0; idx<realityConnectionList.length; idx++)
   	      realityNames[idx] = realityConnectionList[idx].getAliasName();
   	   String[] names = resolveOrderPreferenceVersusReality(realityNames, getConnectionNamesOrder(profileName));
   	   //System.out.println("Connection names resolved preference order for profile " + profileName + "...");
   	   //for (int idx=0; idx<names.length; idx++)
   	   //   System.out.println("   '" + names[idx]+"'");
   	   //System.out.println();
   	   return names;
   }
   /**
    * Set user's preference for the order of the connection names
    */
   public void setConnectionNamesOrder(String[] names)
   {
   	   boolean debug = false;
   	   if (debug)
   	   {
   	     //System.out.println("Saving connection names preference order for all profiles:");
   	     for (int idx=0; idx<names.length; idx++)
   	        System.out.println("   '" + names[idx]+"'");
   	     System.out.println();
   	   }
   	   RemoteSystemsPreferencePage.setConnectionNamesOrder(names);
   }
   /**
    * Set user's preference for the order of the connection names
    */
   public void setConnectionNamesOrder()
   {
   	   ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
   	   IHost[] conns = sr.getHosts();
   	   String[] names = new String[conns.length];
       for (int idx=0; idx<names.length; idx++)
          names[idx] = conns[idx].getSystemProfileName()+"."+conns[idx].getAliasName();
       setConnectionNamesOrder(names);
   }

   /**
    * Helper method to resolve differences between two ordered name lists.
    * Used when there are differences between the actual list of names and
    * a restored ordered list of names.
    */
   public String[] resolveOrderPreferenceVersusReality(String[] reality, String[] ordered)
   {
   	   Vector finalList = new Vector();
   	   // step 1: include all names from preferences list which do exist in reality...
   	   for (int idx=0; idx<ordered.length; idx++)
   	   {
   	   	  if (find(reality, ordered[idx]))
   	   	    finalList.addElement(ordered[idx]);   	   	  
   	   }
   	   // step 2: add all names in reality which do not exist in preferences list...
   	   for (int idx=0; idx<reality.length; idx++)
   	   {
   	   	  if (!find(ordered, reality[idx]))
   	   	    finalList.addElement(reality[idx]);   	   	  
   	   }
   	   String[] resolved = new String[finalList.size()];
   	   for (int idx=0; idx<finalList.size(); idx++)
   	      resolved[idx] = (String)finalList.elementAt(idx);
   	   return resolved;
   }
   
   private boolean find(String[] haystack, String needle)
   {
   	   for (int idx=0; idx<haystack.length; idx++)
   	   {
   	      if (haystack[idx].equals(needle))
   	        return true;
   	   }
   	   return false;
   }

 
   // ------------------
   // USER ID METHODS...
   // ------------------
   
   /**
    * Return user Id per system type
    */
   public String getDefaultUserId(String systemType)
   {
   	   return RemoteSystemsPreferencePage.getUserIdPreference(systemType);
   }
   /**
    * Set user Id per system type
    */
   public void setDefaultUserId(String systemType, String userId)
   {
   	   RemoteSystemsPreferencePage.setUserIdPreference(systemType,userId);
   }


   /**
    * Return user Id per key
    */
   public String getUserId(String key)
   {
   	  String uid = null;
      userIdsPerKey = getUserIdsPerKey();
   	  uid = (String)userIdsPerKey.get(key);
   	  return uid;
   }
   
   /**
	 * Set the user Id for this key. The key typically designates a scope for this userid so that a hierarchy
	 * of user ids can be maintained for inheritance. For example, hosts have greater scope than subsystems.
	 */
	public void setUserId(String key, String userId) {
		if ((key != null) && (userId != null)) {
			userIdsPerKey = getUserIdsPerKey();
			String storedUserId = (String) userIdsPerKey.get(key);
			if (!storedUserId.equals(userId)) { // don't bother updating if its already there
				userIdsPerKey.put(key, userId);
				setUserIdsPerKey();
			}
		}
	}
   
   /**
    * Clear the user Id for the given key
    */
   public void clearUserId(String key)
   {
   	  userIdsPerKey = getUserIdsPerKey();
   	  if (userIdsPerKey.containsKey(key))
   	  {
        userIdsPerKey.remove(key);   
        setUserIdsPerKey();
   	  }
   }

   /**
    * Helper method to get the hashtable of userIds per key. Comes from preference store.
    */
   private Hashtable getUserIdsPerKey()
   {
   	  if (userIdsPerKey == null)
   	    userIdsPerKey = RemoteSystemsPreferencePage.getUserIdsPerKey();
      return userIdsPerKey;
   }
   /**
    * Helper method to set the hashtable of userIds per subsystem. Sets it in the preference store.
    */
   private void setUserIdsPerKey()
   {
   	  RemoteSystemsPreferencePage.setUserIdsPerKey(userIdsPerKey);
   }    

         
   // ----------------------
   // GETTER METHODS...
   // ----------------------
   /**
    * Return whether to show connection names qualified by profile name
    */
   public boolean getQualifyConnectionNames()
   {
   	   return RemoteSystemsPreferencePage.getQualifyConnectionNamesPreference();
   }
   
   /**
    * Return whether to show filter pools or not
    */
   public boolean getShowFilterPools()
   {
   	   return RemoteSystemsPreferencePage.getShowFilterPoolsPreference();
   }

   /**
    * Return whether to remember state or not in Remote Systems view
    */
   public boolean getRememberState()
   {
   	   return RemoteSystemsPreferencePage.getRememberStatePreference();
   }
   /**
    * Return whether to show "New Connection..." prompt
    */
   public boolean getShowNewConnectionPrompt()
   {
   	   return RemoteSystemsPreferencePage.getShowNewConnectionPromptPreference();
   }
   /**
    * Return whether to cascade user actions menu by profile
    */
   public boolean getCascadeUserActions()
   {
   	   return RemoteSystemsPreferencePage.getCascadeUserActionsPreference();
   }
   /**
    * Return whether to turn on "Verify connection" checkbox on the New Connection wizard
    */
   public boolean getVerifyConnection()
   {
   		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(ISystemPreferencesConstants.VERIFY_CONNECTION, ISystemPreferencesConstants.DEFAULT_VERIFY_CONNECTION);
   		return store.getBoolean(ISystemPreferencesConstants.VERIFY_CONNECTION);		
   }
   
   // ----------------------
   // SETTER METHODS...
   // ----------------------
   /**
    * Set whether to show connection names qualified by profile name
    */
   public void setQualifyConnectionNames(boolean set)
   {
   	   RemoteSystemsPreferencePage.setQualifyConnectionNamesPreference(set);
   }

   /**
    * Set whether to show filter pools or not
    */
   public void setShowFilterPools(boolean show)
   {
       boolean prevValue = getShowFilterPools();
   	   RemoteSystemsPreferencePage.setShowFilterPoolsPreference(show);       
       if (show != prevValue)
    	 RSEUIPlugin.getTheSystemRegistry().setShowFilterPools(show);
   }

   /**
    * Set whether to remember state or not in Remote Systems view
    */
   public void setRememberState(boolean remember)
   {
   	   RemoteSystemsPreferencePage.setRememberStatePreference(remember);
   }
   /**
    * Set whether to show "New Connection..." prompt
    */
   public void setShowNewConnectionPrompt(boolean show)
   {
   	   RemoteSystemsPreferencePage.setShowNewConnectionPromptPreference(show);
   }
   /**
    * Set whether to cascade user actions menu by profile
    */
   public void setCascadeUserActions(boolean cascade)
   {
   	   RemoteSystemsPreferencePage.setCascadeUserActionsPreference(cascade);
   }
   /**
    * Set whether to turn on "Verify connection" checkbox on the New Connection wizard
    */
   public void setVerifyConnection(boolean verify)
   {
   		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.VERIFY_CONNECTION, verify);
		RSEUIPlugin.getDefault().savePluginPreferences(); // also saves the preference store
   }
   
   // ------------------
   // HISTORY METHODS...
   // ------------------
    /**
     * Return history for the folder combobox
     */
    public String[] getFolderHistory()
    {
    	return RemoteSystemsPreferencePage.getFolderHistory();
    }
    /**
     * Set history for the folder combobox
     */
    public void setFolderHistory(String[] history)
    {
    	RemoteSystemsPreferencePage.setFolderHistory(history);
    }
    /**
     * Return history for a widget given a key that uniquely identifies it
     */
    public String[] getWidgetHistory(String key)
    {
    	return RemoteSystemsPreferencePage.getWidgetHistory(key);
    }
    /**
     * Set history for a widget given a key that uniquely identifies it
     */
    public void setWidgetHistory(String key, String[] history)
    {
    	RemoteSystemsPreferencePage.setWidgetHistory(key, history);
    }
}