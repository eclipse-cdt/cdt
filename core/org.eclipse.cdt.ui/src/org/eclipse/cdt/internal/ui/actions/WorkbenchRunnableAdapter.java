/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;

import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.ui.CUIStatus;

/**
 * An <code>IRunnableWithProgress</code> that adapts and  <code>IWorkspaceRunnable</code>
 * so that is can be executed inside <code>IRunnableContext</code>. <code>OperationCanceledException</code>
 * thrown by the adapted runnable are caught and re-thrown as a <code>InterruptedException</code>.
 */
public class WorkbenchRunnableAdapter implements IRunnableWithProgress, IThreadListener {
	private IWorkspaceRunnable fWorkspaceRunnable;
	private ISchedulingRule fRule;
	private boolean fTransfer;

	/**
	 * Runs a workspace runnable with the workspace lock.
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable) {
		this(runnable, ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at all.
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule) {
		fWorkspaceRunnable= runnable;
		fRule= rule;
	}

	/**
	 * Runs a workspace runnable with the given lock or <code>null</code> to run with no lock at
	 * all.
	 *
	 * @param runnable the runnable
	 * @param rule the scheduling rule, or <code>null</code>
	 * @param transfer <code>true</code> iff the rule is to be transfered to the modal context
	 *            thread
	 */
	public WorkbenchRunnableAdapter(IWorkspaceRunnable runnable, ISchedulingRule rule, boolean transfer) {
		fWorkspaceRunnable= runnable;
		fRule= rule;
		fTransfer= transfer;
	}

	public ISchedulingRule getSchedulingRule() {
		return fRule;
	}

	@Override
	public void threadChange(Thread thread) {
		if (fTransfer)
			Job.getJobManager().transferRule(fRule, thread);
	}

	/*
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			CoreModel.run(fWorkspaceRunnable, fRule, monitor);
		} catch (OperationCanceledException e) {
			throw new InterruptedException(e.getMessage());
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	public void runAsUserJob(String name, final Object jobFamiliy) {
		Job buildJob = new Job(name){
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					WorkbenchRunnableAdapter.this.run(monitor);
				} catch (InvocationTargetException e) {
					Throwable cause= e.getCause();
					if (cause instanceof CoreException) {
						return ((CoreException) cause).getStatus();
					}
					return CUIStatus.createError(IStatus.ERROR, cause);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
			@Override
			public boolean belongsTo(Object family) {
				return jobFamiliy == family;
			}
		};
		buildJob.setRule(fRule);
		buildJob.setUser(true);
		buildJob.schedule();

		// TODO: should block until user pressed 'to background'
	}
}

