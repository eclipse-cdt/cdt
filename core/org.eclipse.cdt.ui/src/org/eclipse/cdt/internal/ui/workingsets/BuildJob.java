/*******************************************************************************
 * Copyright (c) 2008, 2009 Intel Corporation, QNX Software Systems, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     QNX Software Systems - [272416] Rework the working set configurations
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * A job that builds a bunch of workspace projects or a working set configuration.
 */
public final class BuildJob extends Job {
	private Collection<IProject> projects;
	private IWorkingSetConfiguration workingSetConfig;

	/**
	 * Initializes me with a bunch projects to build in their active configurations.
	 * 
	 * @param projects
	 *            the projects to build
	 */
	public BuildJob(Collection<IProject> projects) {
		super(Messages.WorkingSetConfigAction_21); 
		this.projects = new java.util.ArrayList<IProject>(projects);
	}

	/**
	 * Initializes me with a working set configuration to build.
	 * 
	 * @param workingSetConfig
	 *            the working set configuration to build
	 */
	public BuildJob(IWorkingSetConfiguration workingSetConfig) {
		super(Messages.WorkingSetConfigAction_21); 
		this.workingSetConfig = workingSetConfig;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (projects != null) {
			return buildProjects(monitor);
		} else {
			return buildWorkingSetConfig(monitor);
		}
	}

	private IStatus buildProjects(IProgressMonitor monitor) {
		try {
			for (IProject p : projects) {
				try {
					setName(Messages.WorkingSetConfigAction_21 + p.getName()); 
					p.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
				} catch (CoreException e) {
					return new MultiStatus(CUIPlugin.PLUGIN_ID, 0, new IStatus[] { e.getStatus() },
							Messages.WorkingSetConfigAction_22, 
							null);
				}
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	private IStatus buildWorkingSetConfig(IProgressMonitor monitor) {
		try {
			return workingSetConfig.build(monitor);
		} finally {
			monitor.done();
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
	}

}