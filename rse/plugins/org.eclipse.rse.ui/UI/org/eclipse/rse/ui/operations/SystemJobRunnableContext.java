/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.operations;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;


/**
 * This runnable context executes its operation in the context of a background job.
 */
public final class SystemJobRunnableContext implements ISystemRunnableContext {

	private IJobChangeListener listener;
	private IWorkbenchSite site;
	private String jobName;
	private ISchedulingRule schedulingRule;
	private boolean postponeBuild;
	private boolean isUser;
	private URL icon;
	private boolean keep;
	private IAction action;

	/**
	 * Constructor.
	 * @param jobName the name of the job.
	 */
	public SystemJobRunnableContext(String jobName) {
		this(jobName, null, null, false, null, null);
	}
	
	/**
	 * Constructor.
	 * @param jobName the name of the job.
	 * @param icon the icon for the job.
	 * @param action the action for the job.
	 * @param keep keep the job in the UI even after it is finished.
	 * @param listener listener for job changes.
	 * @param site the workbench site.
	 */
	public SystemJobRunnableContext(String jobName, URL icon, IAction action, boolean keep, IJobChangeListener listener, IWorkbenchSite site) {
		this.jobName = jobName;
		this.listener = listener;
		this.site = site; 
		this.isUser = true;
		this.action = action;
		this.icon = icon;
		this.keep = keep;
	}

	/**
	 * @see org.eclipse.rse.ui.operations.ISystemRunnableContext#run(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(IRunnableWithProgress runnable) {
		
		// the job
		Job job;
		
		// if there is no scheduling rule, and auto-builds do not have to be postponed
		// then use a basic job
		if (schedulingRule == null && !postponeBuild) {
			job = getBasicJob(runnable);
		}
		// otherwise we need a workspace job for which a scheduling rule needs to be set
		else {
			job = getWorkspaceJob(runnable);
			
			// set scheduling rule if it exists
			if (schedulingRule != null) {
				job.setRule(schedulingRule);
			}
		}
		
		// add a job change listener if there is one
		if (listener != null) {
			job.addJobChangeListener(listener);
		}
		
		// sets whether the job is user initiated
		job.setUser(isUser());
		
		// configure the job
		configureJob(job);
		
		// schedult the job
		schedule(job, site);
	}
	
	/**
	 * Configures the properties of the given job.
	 * @param job the job to configure.
	 */
	private void configureJob(Job job) {
		
		// whether to keep the job in the UI after the job has finished to report results
		// back to the user
		if(keep) {
			job.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		}
		
		// an action associated with the job if any
		if(action != null) {
			job.setProperty(IProgressConstants.ACTION_PROPERTY, action);
		}
		
		// an icon associated with the job if any
		if(icon != null) {
			job.setProperty(IProgressConstants.ICON_PROPERTY, icon);
		}
	}

	/**
	 * Returns the shell.
	 * @see org.eclipse.rse.ui.operations.ISystemRunnableContext#getShell()
	 */
	public Shell getShell() {
		return SystemBasePlugin.getActiveWorkbenchShell();
	}
	
	/**
	 * Returns whether auto-builds will be postponed while this
	 * context is executing a runnable.
	 * @return <code>true</code> if auto-builds will be postponed while this
	 * context is executing a runnable, <code>false</code> otherwise.
	 */
	public boolean isPostponeBuild() {
		return postponeBuild;
	}
	
	/**
	 * Sets whether auto-builds will be postponed while this
	 * context is executing a runnable.
	 * @param postponeBuild <code>true</code> to postpone auto-builds, <code>false</code> otherwise.
	 */
	public void setPostponeBuild(boolean postponeBuild) {
		this.postponeBuild = postponeBuild;
	}
	
	/**
	 * Returns the scheduling rule that will be obtained before the context
	 * executes a runnable, or <code>null</code> if no scheduling rule is to be obtained.
	 * @return the schedulingRule to be used or <code>null</code>.
	 */
	public ISchedulingRule getSchedulingRule() {
		return schedulingRule;
	}
	
	/**
	 * Returns whether the job created by this runnable context is user initiated.
	 * @return <code>true</code> if the job is a result of user initiated actions, <code>false</code> otherwise.
	 */
	public boolean isUser() {
		return isUser;
	}
	
	/**
	 * Sets wheter the job created by this runnable context is user initiated.
	 * By default, the job is a user initiated job.
	 * @param isUser <code>true</code> if the job is a result of user initiated actions, <code>false</code> otherwise.
	 */
	public void setUser(boolean isUser) {
		this.isUser = isUser;
	}
	
	/**
	 * Sets the scheduling rule that will be obtained before the context
	 * executes a runnable, or <code>null</code> if no scheduling rule is to be obtained.
	 * @param schedulingRule the scheduling rule to be used or <code>null</code>.
	 */
	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}
	
	/**
	 * Runs the runnable with the given monitor.
	 * @param runnable the runnable.
	 * @param monitor the progress monitor.
	 * @return the status of running the runnable.
	 */
	IStatus run(IRunnableWithProgress runnable, IProgressMonitor monitor) {
		
		// run the runnable
		try {
			runnable.run(monitor);
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			String msg = "";
			
			if (target != null) {
				msg = target.getMessage();
			}
			
			// return an error status
			return new Status(IStatus.ERROR, SystemPlugin.getDefault().getSymbolicName(), 0, msg, target);
		}
		catch (InterruptedException e) {
			return Status.OK_STATUS;
		}
		
		return Status.OK_STATUS;
	}
	
	/**
	 * Returns a basic job which simply runs the runnable.
	 * @param runnable the runnable.
	 * @return the basic job.
	 */
	private Job getBasicJob(final IRunnableWithProgress runnable) {
		return new Job(jobName) {
			public IStatus run(IProgressMonitor monitor) {
				return SystemJobRunnableContext.this.run(runnable, monitor);
			}
		};
	}
	
	/**
	 * Returns a workspace job which simply runs the runnable.
	 * @param runnable the runnable.
	 * @return the workspace job.
	 */
	private Job getWorkspaceJob(final IRunnableWithProgress runnable) {
		return new WorkspaceJob(jobName) {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				return SystemJobRunnableContext.this.run(runnable, monitor);
			}
		};
	}
	
	/**
	 * Schedules the job.
	 * @param job the job to schedule.
	 * @param site the workbench site.
	 */
	public static void schedule(Job job, IWorkbenchSite site) {
		
		if (site != null) {
			
			// get the site progress service
			IWorkbenchSiteProgressService siteProgress = (IWorkbenchSiteProgressService)(site.getAdapter(IWorkbenchSiteProgressService.class));
			
			// if there is one, schedule the job with a half-busy cursor 
			if (siteProgress != null) {
				siteProgress.schedule(job, 0, true);
				return;
			}
		}
		
		// if no site progress service, just schedule the job in the job queue
		job.schedule();
	}
}