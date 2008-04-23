/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved from core package in the UI plugin
 *                     - updated to use new RSEPreferencesManager
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [218655][api] Provide SystemType enablement info in non-UI
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [210474] Deny save password function missing
 ********************************************************************************/

package org.eclipse.rse.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.internal.core.RSECoreMessages;


/**
 * PasswordPersistenceManager manages the saving and retrieving of user ID /
 * passwords to the Eclipse keyring for registered system types.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients. Use
 *                the {@link #getInstance()} method to get the singleton
 *                instance.
 */
public class PasswordPersistenceManager {

	// Keys used for using the Platform authorization methods
	// The server url is generic so we can lookup all registered user IDs / passwords
	// to display to the user in the password information preference page
	private static final String SERVER_URL = "file://rse"; //$NON-NLS-1$

	private static final String AUTH_SCHEME = "";	// no authorization scheme specified for apis  //$NON-NLS-1$

	// Add return codes
	public static final int RC_OK = 0;
	public static final int RC_ALREADY_EXISTS = 1;
	/** @since org.eclipse.rse.core 3.0 */
	public static final int RC_DENIED = 2;
	public static final int RC_ERROR = -1;

	// Default System Type, on a lookup if the specified system type and hostname is not found
	// then the call will automatically lookup the default system type and hostname
	public static final IRSESystemType DEFAULT_SYSTEM_TYPE = new DefaultSystemType();

	// Default user name
	public static final String DEFAULT_USER_NAME = "DEFAULT_USER"; //$NON-NLS-1$

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
	 * Default System Type
	 */
	private static class DefaultSystemType extends AbstractRSESystemType implements IRSESystemType
	{
		private static final String DEFAULT_ID = "DEFAULT"; //$NON-NLS-1$
		private DefaultSystemType() {
			super(DEFAULT_ID, DEFAULT_ID, RSECoreMessages.DefaultSystemType_Label, null, null);
		}
		public String getId() {
			//TODO consider a space character at the beginning to ensure uniqueness
			return DEFAULT_ID;
		}
		public String[] getSubsystemConfigurationIds() {
			return null;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
		public boolean isEnabled() {
			return true;
		}
	}

	/**
	 * Inner class used for storing registered system types
	 */
	private class RegisteredSystemType
	{
		private IRSESystemType _systemType;
		private boolean _userIDCaseSensitive;

		protected RegisteredSystemType(IRSESystemType systemType, boolean caseSensitive)
		{
			_systemType = systemType;
			_userIDCaseSensitive = caseSensitive;
		}

		/**
		 * Returns the system type.
		 * @return the system type.
		 */
		public IRSESystemType getSystemType() {
			return _systemType;
		}

		/**
		 * Returns whether the user ID is case sensitive.
		 * @return <code>true</code> if the user ID is case sensitive, <code>false</code> otherwise.
		 */
		public boolean isUserIDCaseSensitive() {
			return _userIDCaseSensitive;
		}
	}

	/**
	 * Singleton so private constructor
	 */
	private PasswordPersistenceManager(){
		String userName = System.getProperty("user.name"); //$NON-NLS-1$

		if (userName == null) {
			userName = DEFAULT_USER_NAME;
		}

		newURL = SERVER_URL + userName;
	}

	/**
	 * Retrieve the singleton instance of the PasswordPersistenceManger
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
	 * initialization - register system types
	 */
	private void initExtensions()
	{
		IRSESystemType[] sysTypes = RSECorePlugin.getTheCoreRegistry().getSystemTypes();
		systemTypes = new RegisteredSystemType[sysTypes.length];

		for (int i = 0; i < sysTypes.length; i++) {
			systemTypes[i] = new RegisteredSystemType(sysTypes[i], true);
		}
	}

	/**
	 * Remove the entry from the keyring that matches the systemtype, hostname and
	 * user ID from the SystemSignonInfo parameter.
	 */
	public void remove(SystemSignonInformation info)
	{
		remove(info.getSystemType(), info.getHostname(), info.getUserId());
	}

	/**
	 * Removes all passwords for a host name for a given system type. Use the
	 * default system type explicitly to remove those entries.
	 * 
	 * @param systemType The system type of the host
	 * @param hostName The IP address of the host in canonical format
	 * @return the number of passwords removed from the keyring
	 * @since org.eclipse.rse.core 3.0
	 */
	public int remove(IRSESystemType systemType, String hostName) {
		Map passwords = getPasswordMap(systemType);
		int numberRemoved = 0;
		if (passwords != null) {
			String hostPrefix = hostName + "//"; //$NON-NLS-1$
			Set keys = passwords.keySet();
			for (Iterator z = keys.iterator(); z.hasNext();) {
				String key = (String) z.next();
				if (key.startsWith(hostPrefix)) {
					z.remove(); // safely removes the key and the entry from the map
					numberRemoved++;
				}
			}
			if (numberRemoved > 0) {
				savePasswordMap(systemType.getId(), passwords);
			}
		}
		return numberRemoved;
	}

	/**
	 * Removes all entries from the keyring that match the hostname, userid, and system type.
	 * Use the default system type explicitly to remove those entries.
	 * @param systemType the systemType
	 * @param hostName the connection name
	 * @param userid the user id
	 */
	public void remove(IRSESystemType systemType, String hostName, String userid) {
		String hostname = hostName;//RSEUIPlugin.getQualifiedHostName(hname);
		// Convert userid to upper case if required
		if (!isUserIDCaseSensitive(systemType)) {
			userid = userid.toUpperCase();
		}
		Map passwords = getPasswordMap(systemType);
		if (passwords != null) {
			if (removePassword(passwords, hostname, userid)) {
				savePasswordMap(systemType.getId(), passwords);
			}
		}
	}

	/**
	 * Check if a password entry exists for the specified system type, hostname
	 * and userid.
	 */
	public boolean passwordExists(IRSESystemType systemtype, String hostname, String userid)
	{

		return passwordExists(systemtype, hostname, userid, true);
	}

	/**
	 * Check if a password entry exists for the specified system type, hostname
	 * and userid.
	 * 
	 * @param systemtype The system type to check for.
	 * @param hname The hostname to check for.
	 * @param userid The user ID to check for.
	 * @param checkDefault Whether or not to check for a default system type if the specified system type is not found.
	 */
	public boolean passwordExists(IRSESystemType systemtype, String hname, String userid, boolean checkDefault)
	{
		String hostname = hname;//RSEUIPlugin.getQualifiedHostName(hname);
		return (find(systemtype, hostname, userid) != null);
	}

	/**
	 * Add a password to the password database.
	 * This will not update the entry for the default system type
	 * @param info The signon information to store
	 * @param overwrite Whether to overwrite any existing entry
	 * @return
	 * 	RC_OK if the password was successfully stored
	 *  RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 */
	public int add(SystemSignonInformation info, boolean overwrite) {
		return add(info, overwrite, false);
	}

	/**
	 * Add a password to the password database.
	 * @param info The signon information to store
	 * @param overwrite If true then overwrite the existing entry for this systemtype, hostname, and userid.
	 * @param updateDefault if true then set the entry for the default systemtype, hostname, and user ID, according to the overwrite setting.
	 * @return
	 * RC_OK if the password was successfully stored.
	 * RC_ALREADY_EXISTS if the password already exists and overwrite was false
	 * RC_DENIED if passwords may not be saved for this system type and host
	 */
	public int add(SystemSignonInformation info, boolean overwrite, boolean updateDefault) {
		int result = RC_OK;
		IRSESystemType systemType = info.getSystemType();
		String hostName = info.getHostname();
		String userId = info.getUserId();
		String newPassword = info.getPassword();
		boolean deny = RSEPreferencesManager.getDenyPasswordSave(systemType, hostName);
		if (!deny) {
			if (!isUserIDCaseSensitive(systemType)) {
				userId = userId.toUpperCase();
				info.setUserId(userId);
			}
			if (updateDefault) {
				if (systemType != DEFAULT_SYSTEM_TYPE) {
					SystemSignonInformation newInfo = new SystemSignonInformation(hostName, userId, newPassword, DEFAULT_SYSTEM_TYPE);
					result = add(newInfo, overwrite, false);
				}
			}
			Map passwords = getPasswordMap(systemType);
			if (passwords == null) {
				passwords = new HashMap(5);
			}
			String oldPassword = getPassword(passwords, hostName, userId);
			if (oldPassword != null) {
				if (overwrite) {
					removePassword(passwords, hostName, userId);
				} else {
					result = RC_ALREADY_EXISTS;
				}
			}
			if (result == RC_OK) {
				String passwordKey = getPasswordKey(hostName, userId);
				passwords.put(passwordKey, newPassword);
				savePasswordMap(systemType.getId(), passwords);
			}
		} else {
			result = RC_DENIED;
		}
		return result;
	}

	/*
	 * Retrieve the password map from the keyring for the specified system type
	 */
	private Map getPasswordMap(IRSESystemType systemType)
	{
		Map passwords = null;
		String systemTypeId = systemType.getId();

		try
		{
			URL serverURL = new URL(newURL);
			passwords = Platform.getAuthorizationInfo(serverURL, systemTypeId, AUTH_SCHEME);

			// if no passwords found with new URL, check old URL
			if (passwords == null) {

				URL oldServerURL1 = new URL(SERVER_URL + ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
				passwords = Platform.getAuthorizationInfo(oldServerURL1, systemTypeId, AUTH_SCHEME);

				// passwords found, so migrate to using new URL
				if (passwords != null) {
					savePasswordMap(systemTypeId, passwords);
				}
				// if still no passwords found, check with even older URL
				else {
					URL oldServerURL2 = new URL(SERVER_URL);
					passwords = Platform.getAuthorizationInfo(oldServerURL2, systemTypeId, AUTH_SCHEME);

					// passwords found, so migrate to using new URL
					if (passwords != null) {
						savePasswordMap(systemTypeId, passwords);
					}
				}
			}
		}
		catch (MalformedURLException e) {
			RSECorePlugin.getDefault().getLogger().logError("PasswordPersistenceManager.getPasswordMap", e); //$NON-NLS-1$
		}

		return passwords;
	}

	/*
	 * Retrieve the password map from the keyring for the specified system type
	 */
	private void savePasswordMap(String systemTypeId, Map passwords)
	{
		try
		{
			URL serverURL = new URL(newURL);
			Platform.flushAuthorizationInfo(serverURL, systemTypeId, AUTH_SCHEME);
			Platform.addAuthorizationInfo(serverURL, systemTypeId, AUTH_SCHEME, passwords);
		}
		catch (MalformedURLException e) {
			RSECorePlugin.getDefault().getLogger().logError("PasswordPersistenceManager.savePasswordMap", e); //$NON-NLS-1$
		}
		catch (CoreException e) {
			RSECorePlugin.getDefault().getLogger().logError("PasswordPersistenceManager.savePasswordMap", e); //$NON-NLS-1$
		}
	}

	/**
	 * Find the password for the specified systemtype, hostname and userid.
	 * If one is not found then the default system type is used.
	 * The system type in the signon information returned may not be the same as the system type
	 * specfied in the argument.
	 */
	public SystemSignonInformation find(IRSESystemType systemtype, String hostname, String userid)
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
							String qkhost = RSECorePlugin.getQualifiedHostName(khostname);
							String qphost = RSECorePlugin.getQualifiedHostName(phostname);
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
						String qkhost = RSECorePlugin.getQualifiedHostName(khostname);
						String qphost = RSECorePlugin.getQualifiedHostName(phostname);
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
	 * @param hname The hostname to check for.
	 * @param userid The user ID to check for.
	 * @param checkDefault Whether or not to check for a default system type if the specified system type is not found.
	 */
	public SystemSignonInformation find(IRSESystemType systemtype, String hname, String userid, boolean checkDefault)
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
		buffer.append("//"); //$NON-NLS-1$
		buffer.append(userid);
		return buffer.toString();
	}

	private String getHostnameFromPasswordKey(String passwordKey)
	{
		int sepIndex = passwordKey.indexOf("//"); //$NON-NLS-1$
		return passwordKey.substring(0,sepIndex);
	}

	private String getUserIdFromPasswordKey(String passwordKey)
	{
		int sepIndex = passwordKey.indexOf("//"); //$NON-NLS-1$
		return passwordKey.substring(sepIndex + 2, passwordKey.length());
	}

	/**
	 * Helper method for determining if system type uses case sensitive user IDs
	 */
	public boolean isUserIDCaseSensitive(IRSESystemType systemType)
	{
		// First find the correct provider
		for (int i = 0; i < systemTypes.length; i++)
		{

			if (systemTypes[i].getSystemType().equals(systemType))
			{
				return systemTypes[i].isUserIDCaseSensitive();
			}
		}

		//Not found: Default system type is case sensitive
		return true;
	}

	/**
	 * Retrieve the list of registered system types
	 */
	public IRSESystemType[] getRegisteredSystemTypes()
	{
		// yantzi: artemis 6.2, added default system type to list
		IRSESystemType[] types = new IRSESystemType[systemTypes.length + 1];

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
					separator = key.indexOf("//"); //$NON-NLS-1$
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
				separator = key.indexOf("//"); //$NON-NLS-1$
				savedUserIDs.add(new SystemSignonInformation(key.substring(0, separator),		// hostname
						key.substring(separator + 2),		// userid
						DEFAULT_SYSTEM_TYPE)); 	// system type
			}
		}

		return savedUserIDs;
	}

}