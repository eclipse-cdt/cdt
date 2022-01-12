/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job without rule to setup the indexer.
 */
public class PDOMSetupJob extends Job {
	private static final String SETTINGS_FOLDER_NAME = ".settings"; //$NON-NLS-1$

	private PDOMManager fManager;

	PDOMSetupJob(PDOMManager manager) {
		super(Messages.PDOMManager_StartJob_name);
		fManager = manager;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		while (true) {
			ICProject cproject = fManager.getNextProject();
			if (cproject == null)
				return Status.OK_STATUS;

			final IProject project = cproject.getProject();
			monitor.setTaskName(project.getName());
			if (!project.isOpen()) {
				if (fManager.fTraceIndexerSetup)
					System.out.println("Indexer: Project is not open: " + project.getName()); //$NON-NLS-1$
			} else if (fManager.postponeSetup(cproject)) {
				if (fManager.fTraceIndexerSetup)
					System.out.println("Indexer: Setup is postponed: " + project.getName()); //$NON-NLS-1$
			} else {
				syncronizeProjectSettings(project, progress.newChild(1));
				if (fManager.getIndexer(cproject) == null) {
					try {
						fManager.createIndexer(cproject, progress.newChild(99));
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return Status.CANCEL_STATUS;
					}
				} else if (fManager.fTraceIndexerSetup) {
					System.out.println("Indexer: No action, indexer already exists: " + project.getName()); //$NON-NLS-1$
				}
			}
		}
	}

	private void syncronizeProjectSettings(IProject project, IProgressMonitor monitor) {
		try {
			IFolder settings = project.getFolder(SETTINGS_FOLDER_NAME);
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
