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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;

public class RSEUIInitJob extends Job {

	public RSEUIInitJob() {
		super("RSE_UI_INIT"); //$NON-NLS-1$
	}

	public IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			String message = "UI Initialization interrupted"; //$NON-NLS-1$
			status = new Status(IStatus.INFO, RSEUIPlugin.PLUGIN_ID, message);
			SystemBasePlugin.logInfo(message);
		}
		// listen for project change events if a project is being used
		IProject remoteSystemsProject = SystemResourceManager.getRemoteSystemsProject(false);
		if (remoteSystemsProject.exists()) {
			SystemResourceListener listener = SystemResourceListener.getListener(remoteSystemsProject);
			SystemResourceManager.startResourceEventListening(listener);
		}
		return status;
	}

}