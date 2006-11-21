/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.systems.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.model.ISystemRegistryUI;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemRegistry;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * SystemTestUtil is a collection of static utility methods for creating 
 * RSE system connections and associated RSE artifacts (filter pools, filters, etc...) to assist
 * you in writing your JUnit plug-in testcases.
 * <p>
 * Since most JUnit PDE testcases create a brand new workspace when they start you will likely need
 * to create a new RSE connection to start your testing.  The "createSystemConnection(...) methods
 * are therefore your most likely starting point.  
 * <p>
 * Note:  If your testcases subclasses AbstractSystemConnectionTest then you can use the getConnection()
 * method instead.   
 * 
 * @author yantzi
 */
public class SystemTestUtil {

	/**
	 * Load a properties file and return the Properties object.
	 * @param bundle The bundle containing the properties.
	 * @param propertiesFileName the properties file name relative to the bundle. 
	 * @return the Properties object, may be empty if no such file is found or an error occurs.
	 */
	public static Properties loadProperties(Bundle bundle, String propertiesFileName) {
		URL url = bundle.getEntry(propertiesFileName);
		Properties result = new Properties();
		try {
			InputStream in = url.openStream();
			result.load(in);
			in.close();
		} catch (IOException e) {
		}
		return result;
	}

	/**
	 * @param profileName The profile in which to look for the host.
	 * @param hostName The host to look for.
	 * @return The requested host (connection) or null if none was found.
	 */
	public static IHost findHost(String profileName, String hostName) {
		IHost host = null;
		ISystemRegistryUI registry = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile profile = registry.getSystemProfile(profileName);
		if (profile != null) {
			host = registry.getHost(profile, hostName);
		}
		return host;
	}

	/**
	 * Create a new system connection.
	 * If a user ID and password are not provided then the testcase will pause while the user 
	 * is prompted to signon.
	 * 
	 * @param profileName The name of an existing RSE profile under which this connection should be created.
	 * @param hostName The name for the new RSE connection.
	 * @param hostAddress The IP address or name for the new RSE connection.
	 * @param systemType the system type of the new connection.
	 * @param userid The user ID for the new RSE connection. May be null.
	 * @param password The password to be used in conjunction with the user ID for 
	 * connecting to the remote system. May be null.
	 * @return A new RSE IHost for the specified host information 
	 * @throws Exception
	 */
	public static IHost createHost(String profileName, String hostName, String hostAddress, String systemType, String userid, String password) throws Exception {
		ISystemRegistryUI registry = RSEUIPlugin.getTheSystemRegistry();
		IHost connection = registry.createHost(profileName, systemType, hostName, hostAddress, null, userid, ISystemUserIdConstants.USERID_LOCATION_CONNECTION, null);
		if (userid != null && password != null) {
			savePassword(hostAddress, userid, password, systemType); // register password for this hostname
		}
		return connection;
	}

	/**
	 * Delete a host given its name and the name of its profile. If the host is not found then
	 * do nothing.
	 * @param profileName the name of the profile containing the host
	 * @param hostName the name of the host to delete
	 */
	public static void deleteHost(String profileName, String hostName) {
		IHost host = findHost(profileName, hostName);
		if (host != null) {
			SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.deleteHost(host);
			registry.fireEvent(new SystemResourceChangeEvent(host, ISystemResourceChangeEvents.EVENT_DELETE, registry));
		}
	}

	/**
	 * Find a profile given its name.
	 * @param profileName the name of the profile to find
	 * @return the ISystemProfile that was found.
	 */
	public static ISystemProfile findProfile(String profileName) {
		ISystemRegistryUI registry = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile profile = registry.getSystemProfile(profileName);
		return profile;
	}

	/**
	 * Creates a new profile. If the profile already exists, it throws an exception.
	 * @param profileName The name of the profile to create.
	 * @return The profile that was created.
	 * @throws RuntimeException if the profile exists or it cannot be created.
	 */
	public static ISystemProfile createProfile(String profileName) {
		ISystemRegistryUI registry = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile profile = findProfile(profileName);
		if (profile != null) {
			throw new RuntimeException(MessageFormat.format("Profile {0} already exists.", new Object[] { profileName }));
		}
		try {
			profile = registry.createSystemProfile(profileName, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return profile;
	}

	/**
	 * Save the password assocaited with the specified hostname and userid.  This method overwrites any previously
	 * saved password for the specified hostname and user ID.
	 * 
	 * @param hostname The hostname to save the password for.
	 * @param userid The user ID to save the password for.
	 * @param password The password to be saved.
	 * @param systemtype the system type of the new connection
	 * 
	 * @return true if the password was saved okay or false if it was not able to be saved
	 */
	public static boolean savePassword(String hostname, String userid, String password, String systemtype) {
		SystemSignonInformation info = new SystemSignonInformation(hostname, userid, password, systemtype);
		return (PasswordPersistenceManager.getInstance().add(info, true) == PasswordPersistenceManager.RC_OK);
	}

	/**
	 * Retrieve the default RSE system profile.  If the default profile has not been renamed from the default
	 * name ("Private") then the profile is renamed to the DEFAULT_PROFILE_NAME specified in 
	 * SystemConnectionTests.properties.
	 * @param profileName the name the default profile will become.
	 * @return The default RSE system profile.
	 * @throws Exception of the profile cannot be found
	 */
	public static ISystemProfile getDefaultProfile(String profileName) throws Exception {
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile defaultProfile = sr.getSystemProfileManager().getDefaultPrivateSystemProfile();
		if (defaultProfile != null && defaultProfile.getName().equals("Private")) {
			sr.renameSystemProfile(defaultProfile, profileName);
		}
		return defaultProfile;
	}

	/**
	 * Rename the default RSE system profile.
	 * 
	 * @param name The new name for the default RSE system profile.
	 * 
	 * @return The default RSE system profile
	 * @throws Exception if the profile cannot be renamed
	 */
	public static ISystemProfile renameDefaultProfile(String name) throws Exception {
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		ISystemProfile defaultProfile = sr.getSystemProfileManager().getDefaultPrivateSystemProfile();
		if (defaultProfile != null) {
			sr.renameSystemProfile(defaultProfile, name);
		}
		return defaultProfile;
	}

	/**
	 * Display a simple String message to the user.  This can be used to provide testing instructions to the user
	 * to guide them through semi-automated test cases. 
	 * @param shell the shell on which to show the message
	 * @param message the message to show
	 */
	public static void displayMessage(Shell shell, String message) {
		MessageBox msgBox = new MessageBox(shell);
		msgBox.setMessage(message);
		msgBox.open();
	}

}
