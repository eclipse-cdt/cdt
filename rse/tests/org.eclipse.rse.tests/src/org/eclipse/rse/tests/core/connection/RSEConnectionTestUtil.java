/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Don Yantzi (IBM) - initial contribution.
 * David Dykstal (IBM) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.core.connection;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;

/**
 * RSEConnectionTestUtil is a collection of static utility methods for creating 
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
public class RSEConnectionTestUtil {



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
		if (defaultProfile != null && defaultProfile.getName().equals("Private")) { //$NON-NLS-1$
			sr.renameSystemProfile(defaultProfile, profileName);
		}
		return defaultProfile;
	}

}
