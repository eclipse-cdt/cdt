/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job without rule to setup the indexer.
 */
public class PDOMSetupJob extends Job {
	private static final String SETTINGS_FOLDER_NAME = ".settings"; //$NON-NLS-1$

	private PDOMManager fManager;

	PDOMSetupJob(PDOMManager manager) {
		super(Messages.PDOMManager_StartJob_name);
		fManager= manager;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		while(true) {
			ICProject cproject= fManager.getNextProject();
			if (cproject == null)
				return Status.OK_STATUS;
			
			final IProject project= cproject.getProject();
			monitor.setTaskName(project.getName());
			if (project.isOpen() && !fManager.postponeSetup(cproject)) {
				syncronizeProjectSettings(project, new SubProgressMonitor(monitor, 1));
				if (fManager.getIndexer(cproject) == null) {
					try {
						fManager.createIndexer(cproject, new SubProgressMonitor(monitor, 99));
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					}
				}
			}
		}
	}

	private void syncronizeProjectSettings(IProject project, IProgressMonitor monitor) {
		try {
			IFolder settings= project.getFolder(SETTINGS_FOLDER_NAME);  
			settings.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		monitor.done();
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == fManager;
	}
}
