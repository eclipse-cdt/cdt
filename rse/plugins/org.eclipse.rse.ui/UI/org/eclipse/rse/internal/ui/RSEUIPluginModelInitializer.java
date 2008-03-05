/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [197167] initial contribution.
 *********************************************************************************/

package org.eclipse.rse.internal.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.IRSEModelInitializer;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemProfileManager;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;

public class RSEUIPluginModelInitializer implements IRSEModelInitializer {

	private boolean isComplete = false;

	public IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// create a local host object if one is desired and one has not yet been created in this workspace.
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IPath statePath = RSECorePlugin.getDefault().getStateLocation();
		IPath markPath = statePath.append("localHostCreated.mark"); //$NON-NLS-1$
		File markFile = new File(markPath.toOSString());
		if (!markFile.exists() && SystemPreferencesManager.getShowLocalConnection()) {
			// create the connection only if the local system type is enabled
			IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_LOCAL_ID);
			if (systemType != null && systemType.isEnabled()) {
				ISystemProfileManager profileManager = RSECorePlugin.getTheSystemProfileManager();
				ISystemProfile profile = profileManager.getDefaultPrivateSystemProfile();
				String userName = System.getProperty("user.name"); //$NON-NLS-1$
				registry.createLocalHost(profile, SystemResources.TERM_LOCAL, userName);
				try {
					markFile.createNewFile();
				} catch (IOException e) {
					status = new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, "IOException creating mark file during local host creation", e); //$NON-NLS-1$
				}
			}
		}
		monitor.done();
		// listen for project change events if a project is being used
		IProject remoteSystemsProject = SystemResourceManager.getRemoteSystemsProject(false);
		if (remoteSystemsProject.exists()) {
			SystemResourceListener listener = SystemResourceListener.getListener(remoteSystemsProject);
			SystemResourceManager.startResourceEventListening(listener);
		}
		isComplete = true;
		return status;
	}

	public boolean isComplete() {
		return isComplete;
	}
}