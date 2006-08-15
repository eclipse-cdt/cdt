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

package org.eclipse.rse.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.model.SystemSignonInformation;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * PasswordPersistenceManager manages the saving and retreiving of user ID / passwords
 * to the Eclipse keyring for registered system types.  Registration is done via the
 * org.eclipse.rse.core.passwordPersistence extension point.
 * 
 * @author yantzi
 */
public class PasswordPersistenceManager {


	// Keys used for using the Platform authorization methods
	// The server url is generic so we can lookup all registered user IDs / passwords
	// to display to the user in the password information preference page
	private static final String SERVER_URL = "file://rse";

	private static final String AUTH_SCHEME = "";	// no authorization scheme specified for apis
	
	// Add return codes
	public static final int RC_OK = 0;
	public static final int RC_ALREADY_EXISTS = 1;
	public static final int RC_ERROR = -1;	
	
	// Default System Type, on a lookup if the specified system type and hostname is not found
	// then the call will automatically lookup the default system type and hostname
	public static final String DEFAULT_SYSTEM_TYPE = "DEFAULT";
	
	// Default user name
	public static final String DEFAULT_USER_NAME = "DEFAULT_USER";
	
	// New URL to store password map
	private String newURL = null;
	
	/*
	 * Singleton instance
	 */
	private static PasswordPersistenceManager _instance;	

	/*
	 * Instance variables
	 */
	private RegisteredSystemType[] systemTypes;
	
	/**
	 * Inner class used for storing registered system types 
	 */
	private class RegisteredSystemType
	{
		private String _systemType;
		private boolean _userIDCaseSensitive;

		protected RegisteredSystemType(String systemType, boolean caseSensitive)
		{
			_systemType = systemType;
			_userIDCaseSensitive = caseSensitive;
		}		
		
		/**
		 * @return
		 */
		public String getSystemType() {
			return _systemType;
		}

		/**
		 * @return
		 */
		public boolean isUserIDCaseSensitive() {
			return _userIDCaseSensitive;
		}
	}	

	/**
	 * Singleton so private constructor
	 */
	private PasswordPersistenceManager(){
		String userName = System.getProperty("user.name");
		
		if (userName == null) {
			userName = DEFAULT_USER_NAME;
		}
		
		newURL = SERVER_URL + userName;
	}
	
	/**
	 * Retrieve the singleton isntance of the PasswordPersistenceManger
	 */
	public static final synchronized PasswordPersistenceManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new PasswordPersistenceManager();
			_instance.initExtensions();
		}
		return _instance;
	}

	/*
	 * initialization
	 * 		- read password file
	 *  	- load IPasswordEncryptionProvider instances
	 */
	private void initExtensions()
	{
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.rse.ui.passwordPersistence");
		
		if (point != null) 
		{
      		IExtension[] extensions = point.getExtensions();  		
      		systemTypes = new RegisteredSystemType[extensions.length];
      		
			int count = 0;      		
      		String systemType, caseSensitiveAsString;
      		boolean caseSensitive;
      		
			for (int i = 0; i < extensions.length; i++) 
			{
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				if (elements != null && elements.length > 0)
				{
				    if ("provider".equals(elements[0].getName())) 
				    {
				    	// Make sure that all attributes are available
				    	systemType = elements[0].getAttribute("systemType");
				    	caseSensitiveAsString = elements[0].getAttribute("caseSensitive");
				    	if (caseSensitiveAsString != null && caseSensitiveAsString.equals("false"))
				    	{
				    		caseSensitive = false;
				    	}
				    	else
				    	{
				    		caseSensitive = true;
				    	}
				    	
						systemTypes[count] = new RegisteredSystemType(systemType, caseSensitive);
						count++;
				    }
				}
				else
				{
					SystemBasePlugin.logError("PasswordPersistenceManager.init:  Invalid extension point", null);
				}
			}

			// Resize array if one or more of the extension points was invalid
			if (count != extensions.length)
			{
				RegisteredSystemType[] temp = new RegisteredSystemType[count];
	      		for (int i = 0; i < count; i++)
	      		{
	      			temp[i] = systemTypes[i];
	      		}
				systemTypes = temp;				
			}
		}
		else
		{	
			SystemBasePlugin.logError("PasswordPersistenceManager.init:  extension point not found", null);
		}   		
	}
		
	/**
	 * Remove the entry from the keyring that matches the systemtype, hostname and
	 * user ID from the SystemSignonInfo parameter.
	 */
	public void remove(SystemSignonInformation info)
	{
		remove(info.getSystemType(), info.getHostname(), info.getUserid());
	}	
	
	/**
	 * Remove the entry from the keyring that matches the hostname, userid and
	 * system type parameters.	 
	 */
	public void remove(String systemtype, String hname, String userid)
	{
		String hostname = hname;//RSEUIPlugin.getQualifiedHostName(hname);
		// Convert userid to upper case if required
		if (!isUserIDCaseSensitive(systemtype))
		{
			userid = userid.toUpperCase();
		}

		Map passwords = getPasswordMap(systemtype);
		
		if (passwords != null)
		{
			if (removePassword(passwords, hostname, userid))
			{
				savePasswordMap(systemtype, passwords);
			}
		}		
		else
		{
			// yantzi: RSE6.2 check for default system type entry with this hostname and user ID
			if (!DEFAULT_SYSTEM_TYPE.equals(systemtype))
			{
				remove(DEFAULT_SYSTEM_TYPE, hostname, userid);
			}
		}
	}
				
	/**
	 * Check if a password entry exists for the specified system type, hostname
	 * and userid.
	 */
	public boolean passwordExists(String systemtype, String hostname, String userid)
	{
	
		return passwordExists(systemtype, hostname, userid, true);
	}

	/**
	 * Check if a password entry exists for the specified system type, hostname
	 * and userid.
	 * 
	 * @param systemtype The system type to check for.
	 * @param hotname The hostname to check for.
	 * @param userid The user ID to check for.
	 * @param checkDefault Whether or not to check for a default system type if the specified system type is not found.
	 * 
	 * @since RSE 6.2
	 */
	public boolean passwordExists(String systemtype, String hname, String userid, boolean checkDefault)
	{
		String hostname = hname;//RSEUIPlugin.getQualifiedHostName(hname);
		return (find(systemtype, hostname, userid) != null);
	}

	/**
	 * Add a new persisted password to the password database.  This method assumes
	 * the encrypted password is already stored in the SystemSignonInformation
	 * parameter.
	 * 
	 * @param info The signon information to store
	 * @param overwrite Whether to overwrite any existing entry
	 * 
	 * @return 
	 * 	RC_OK if the password was successfully stored
	 *  RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 */
	public int add(SystemSignonInformation info, boolean overwrite)
	{
		return add(info, overwrite, false);
	}
	
	/**
	 * Add a new persisted password to the password database.  This method assumes
	 * the encrypted password is already stored in the SystemSignonInformation
	 * parameter.
	 * 
	 * @param info The signon information to store
	 * @param overwrite Whether to overwrite any existing entry
	 * @param updateDefault Whether or not to update the default entry for the specified hostname / user ID if one exists.
	 * 
	 * @return 
	 * 	RC_OK if the password was successfully stored
	 *  RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 */
	public int add(SystemSignonInformation info, boolean overwrite, boolean updateDefault)
	{
		String systemtype = info.getSystemType();
		
		// Convert userid to upper case if required
		if (!isUserIDCaseSensitive(systemtype))
		{
			info.setUserid(info.getUserid().toUpperCase());
		}

		String hostname = info.getHostname();
		String userid = info.getUserid();
		Map passwords = getPasswordMap(systemtype);
		String passwordKey = getPasswordKey(hostname, userid);
	
		if (passwords != null)
		{
			String password = getPassword(passwords, hostname, userid);

			if (password != null)
			{				
				if (!overwrite)
				{
					return RC_ALREADY_EXISTS;
				}
				else
				{
					removePassword(passwords, hostname, userid);
				}
			}
			else if (updateDefault)
			{
				// yantzi: 6.2, check if default exists for the specified hostname / user ID
				Map defaultPasswords = getPasswordMap(DEFAULT_SYSTEM_TYPE);
				if (defaultPasswords != null)
				{
					String defaultPassword = (String) defaultPasswords.get(passwordKey);
					if (defaultPassword != null)
					{
						if (!overwrite)
						{
							return RC_ALREADY_EXISTS;
						}
						else
						{
							defaultPasswords.remove(passwordKey);
							passwords = defaultPasswords;
							systemtype = DEFAULT_SYSTEM_TYPE;
						}
					}
				}
			}
		}
		else
		{
			// password map did not exists yet so create a new one
			passwords = new HashMap(5);	
		}
		
		passwords.put(passwordKey, info.getPassword());
		
		savePasswordMap(systemtype, passwords);		
		
		return RC_OK;
	}

		
	/*
	 * Retrieve the password map from the keyring for the specified system type
	 */		
	private Map getPasswordMap(String systemType)
	{
		Map passwords = null;
		
		try
		{
			URL serverURL = new URL(newURL);
			passwords = Platform.getAuthorizationInfo(serverURL, systemType, AUTH_SCHEME);
			
			// if no passwords found with new URL, check old URL
			if (passwords == null) {
				
				URL oldServerURL1 = new URL(SERVER_URL + SystemBasePlugin.getWorkspace().getRoot().getLocation().toOSString());
				passwords = Platform.getAuthorizationInfo(oldServerURL1, systemType, AUTH_SCHEME);
				
				// passwords found, so migrate to using new URL
				if (passwords != null) {
					savePasswordMap(systemType, passwords);
				}
				// if still no passwords found, check with even older URL
				else {
					URL oldServerURL2 = new URL(SERVER_URL);
					passwords = Platform.getAuthorizationInfo(oldServerURL2, systemType, AUTH_SCHEME);
				
					// passwords found, so migrate to using new URL
					if (passwords != null) {
						savePasswordMap(systemType, passwords);						
					}
				}
			}
		}
		catch (MalformedURLException e) {
			SystemBasePlugin.logError("PasswordPersistenceManager.getPasswordMap", e);
		}
		
		return passwords; 
	}

	/*
	 * Retrieve the password map from the keyring for the specified system type
	 */		
	private void savePasswordMap(String systemType, Map passwords)
	{
		try
		{
			URL serverURL = new URL(newURL);
			Platform.flushAuthorizationInfo(serverURL, systemType, AUTH_SCHEME);
			Platform.addAuthorizationInfo(serverURL, systemType, AUTH_SCHEME, passwords);
		}
		catch (MalformedURLException e) {
			SystemBasePlugin.logError("PasswordPersistenceManager.savePasswordMap", e);
		}
		catch (CoreException e) {
			SystemBasePlugin.logError("PasswordPersistenceManager.savePasswordMap", e);
		}		
	}

	/**
	 * Find the persisted password for the specified systemtype, hostname and userid. 
	 */
	public SystemSignonInformation find(String systemtype, String hostname, String userid)
	{
		return find(systemtype, hostname, userid, true);
	}
	
	
	private boolean removePassword(Map passwords, String hostname, String userid)
	{
		boolean removed = false;
		String password = null;
	
			String passwordKey = getPasswordKey(hostname, userid);
			password =(String) passwords.get(passwordKey);
			if (password != null)
			{
				passwords.remove(passwordKey);
				removed = true;
			}
			else
			{			
				String phostname = hostname.toUpperCase();	
				
				// DKM - fallback for different case uids, hostnames or qualified/unqualified hostnames
				Iterator keys = passwords.keySet().iterator();
				while (keys.hasNext() && password == null)
				{
					String key = (String)keys.next();
					if (key.equalsIgnoreCase(passwordKey))
					{
						password = (String) passwords.get(key);
					}
					else
					{
						String khostname = getHostnameFromPasswordKey(key).toUpperCase();
						String kuid = getUserIdFromPasswordKey(key);
						if (kuid.equalsIgnoreCase(userid))
						{
							// uid matches, check if hosts are the same
							if (khostname.startsWith(phostname) || phostname.startsWith(khostname))
							{
								String qkhost = RSEUIPlugin.getQualifiedHostName(khostname);
								String qphost = RSEUIPlugin.getQualifiedHostName(phostname);
								if (qkhost.equals(qphost))
								{
									password = (String)passwords.get(key);
								}
							}
						}
					}
					if (password != null)
					{
						passwords.remove(key);
						removed = true;
						
				}
			}
		}
		return removed;

	}
	
	private String getPassword(Map passwords, String hostname, String userid)
	{
		String password = null;

			String passwordKey = getPasswordKey(hostname, userid);
			password =(String) passwords.get(passwordKey);
			if (password != null)
				return password;
			
			String phostname = hostname.toUpperCase();	
			
			// DKM - fallback for different case uids, hostnames or qualified/unqualified hostnames
			Iterator keys = passwords.keySet().iterator();
			while (keys.hasNext() && password == null)
			{
				String key = (String)keys.next();
				if (key.equalsIgnoreCase(passwordKey))
				{
					password = (String) passwords.get(key);
				}
				else
				{
					String khostname = getHostnameFromPasswordKey(key).toUpperCase();
					String kuid = getUserIdFromPasswordKey(key);
					if (kuid.equalsIgnoreCase(userid))
					{
						// uid matches, check if hosts are the same
						if (khostname.startsWith(phostname) || phostname.startsWith(khostname))
						{
							String qkhost = RSEUIPlugin.getQualifiedHostName(khostname);
							String qphost = RSEUIPlugin.getQualifiedHostName(phostname);
							if (qkhost.equals(qphost))
							{
								password = (String)passwords.get(key);
							}
						}
					}
				}
			}
		
		return password;

	}
	
	/**
	 * Find the persisted password for the specified systemtype, hostname and userid.
	 *  
	 * @param systemtype The system type to check for.
	 * @param hotname The hostname to check for.
	 * @param userid The user ID to check for.
	 * @param checkDefault Whether or not to check for a default system type if the specified system type is not found.
	 * 
	 * @since RSE 6.2
	 */
	public SystemSignonInformation find(String systemtype, String hname, String userid, boolean checkDefault)
	{
		String hostname = hname;//RSEUIPlugin.getQualifiedHostName(hname);
		// Convert userid to upper case if required
		if (!isUserIDCaseSensitive(systemtype) && userid != null)
		{
			userid = userid.toUpperCase();
		}

		Map passwords = getPasswordMap(systemtype);
		
		if (passwords != null)
		{
			String password = getPassword(passwords, hostname, userid);

			if (password != null)
			{
				return new SystemSignonInformation(hostname, userid, password, systemtype);
			}
		}	
		
		// yantzi: RSE6.2 check for default system type entry with this hostname and user ID
		if (checkDefault && !DEFAULT_SYSTEM_TYPE.equals(systemtype))
		{
			return find(DEFAULT_SYSTEM_TYPE, hostname, userid, false);
		}
		
		return null;	
	}
		
	/**
	 * Helper class for building the key to lookup the password for a specific
	 * userid and hostname in the Map 
	 */
	private String getPasswordKey(String hname, String userid)
	{
		String hostname = hname;//RSEUIPlugin.getQualifiedHostName(hname);
		StringBuffer buffer = new StringBuffer(hostname);
		buffer.append("//");
		buffer.append(userid);
		return buffer.toString();
	}
	
	private static String getHostnameFromPasswordKey(String passwordKey)
	{
		int sepIndex = passwordKey.indexOf("//");
		return passwordKey.substring(0,sepIndex);
	}
	
	private static String getUserIdFromPasswordKey(String passwordKey)
	{
		int sepIndex = passwordKey.indexOf("//");
		return passwordKey.substring(sepIndex + 2, passwordKey.length());
	}
	
	/**
	 * Helper method for determining if system type uses case sensitive user IDs
	 */
	public boolean isUserIDCaseSensitive(String systemType)
	{
		if (DEFAULT_SYSTEM_TYPE.equals(systemType))
		{
			IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
			systemType = store.getString(ISystemPreferencesConstants.SYSTEMTYPE);
		}
	
		// First find the correct provider
		for (int i = 0; i < systemTypes.length; i++)
		{			
			
			if (systemTypes[i].getSystemType().equals(systemType))
			{
				return systemTypes[i].isUserIDCaseSensitive();
			}
		}

		return true;
	}

	/**
	 * Retrieve the list of registered system types
	 */	
	public String[] getRegisteredSystemTypes()
	{
		// yantzi: artemis 6.2, added default system type to list
		String[] types = new String[systemTypes.length + 1];
		
		types[0] = DEFAULT_SYSTEM_TYPE;
		
		for (int i = 0; i < systemTypes.length; i++)
		{
			types[i + 1] = systemTypes[i].getSystemType();
		}
		
		return types;
	}
	
	/**
	 * Retrieve a list of the stored user IDs.
	 * 
	 * @return List A list of the stored user IDs as SystemSignonInformation instances
	 * without the saved passwords. 
	 */
	public List getSavedUserIDs()
	{
		List savedUserIDs = new ArrayList();
		Map passwords;
		String key;
		int separator;
		
		for (int i = 0; i < systemTypes.length; i++)
		{
			passwords = getPasswordMap(systemTypes[i].getSystemType());
			if (passwords != null)
			{
				Iterator keys = passwords.keySet().iterator();
				while (keys.hasNext())
				{
					key = (String) keys.next();
					separator = key.indexOf("//");
					savedUserIDs.add(new SystemSignonInformation(key.substring(0, separator),		// hostname 
																 key.substring(separator + 2),		// userid
																 systemTypes[i].getSystemType())); 	// system type
				}
			}
		}
		
		// yantzi:  RSE 6.2 Get DEFAULT system types too
		passwords = getPasswordMap(DEFAULT_SYSTEM_TYPE);
		if (passwords != null)
		{
			Iterator keys = passwords.keySet().iterator();
			while (keys.hasNext())
			{
				key = (String) keys.next();
				separator = key.indexOf("//");
				savedUserIDs.add(new SystemSignonInformation(key.substring(0, separator),		// hostname 
															 key.substring(separator + 2),		// userid
															 DEFAULT_SYSTEM_TYPE)); 	// system type
			}
		}
		
		return savedUserIDs;
	}

}