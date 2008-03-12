/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] adding initializer support to startup
 * David Dykstal (IBM) - [222376] NPE if starting on a workspace with an old mark and a renamed default profile
 ********************************************************************************/
package org.eclipse.rse.internal.core;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSEModelInitializer;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSELocalConnectionInitializer implements IRSEModelInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.IRSEModelInitializer#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// look for the old style mark
		IPath pluginState = RSECorePlugin.getDefault().getStateLocation();
		IPath markPath = pluginState.append("localHostCreated.mark"); //$NON-NLS-1$
		File markFile = new File(markPath.toOSString());
		boolean markExists = markFile.exists();
		if (!markExists) {
			// create a local host object only if an old style mark does not exist
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			ISystemProfileManager profileManager = RSECorePlugin.getTheSystemProfileManager();
			ISystemProfile profile = profileManager.getDefaultPrivateSystemProfile();
			String localConnectionName = RSECoreMessages.RSELocalConnectionInitializer_localConnectionName;
			IHost localHost = registry.getHost(profile, localConnectionName);
			if (localHost == null && RSEPreferencesManager.getCreateLocalConnection()) {
				// create the connection only if the local system type is enabled
				IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
				if (systemType != null && systemType.isEnabled()) {
					String userName = System.getProperty("user.name"); //$NON-NLS-1$
					registry.createLocalHost(profile, localConnectionName, userName);
				}
			}
		}
		monitor.done();
		return status;
	}

}
