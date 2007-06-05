/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.tests.framework.TestFrameworkPlugin;
import org.eclipse.rse.tests.framework.AbstractTestSuiteHolder;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * This implements the delegate for the "Run Test Suite" action presented in the
 * UI. The UI Proxy invokes this delegate.
 * 
 * @see IObjectActionDelegate
 */
public class RunHolderDelegate implements IObjectActionDelegate {
	private IWorkbenchPart part;
	private ISelection selection;

	/**
	 * The constructor.
	 */
	public RunHolderDelegate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
	 *      org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IPreferenceStore store = TestFrameworkPlugin.getDefault().getPreferenceStore();
		boolean runInBackground = store.getBoolean(TestFrameworkPlugin.PREF_RUN_IN_BACKGROUND);
		if (runInBackground) {
			runInBackground();
		} else {
			runInUI();
		}
	}
	
	private void runInBackground() {
		Job job = new Job("Running JUnit Tests Suites") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStatus result = runTests(monitor);
				return result;
			}
		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		IWorkbenchPartSite site = part.getSite();
		IWorkbenchSiteProgressService siteService = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
		siteService.schedule(job, 0, true);
	}
	
	private void runInUI() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				runTests(monitor);
			}
		};
		IWorkbenchPartSite site = part.getSite();
		IWorkbenchSiteProgressService siteService = (IWorkbenchSiteProgressService) site.getAdapter(IWorkbenchSiteProgressService.class);
		try {
			siteService.runInUI(siteService, runnable, null);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}
	
	private IStatus runTests(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			monitor.beginTask("", ss.size()); //$NON-NLS-1$
			for (Iterator z = ss.iterator(); z.hasNext();) {
				AbstractTestSuiteHolder holder = (AbstractTestSuiteHolder) z.next();
				monitor.subTask(holder.getName());
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				holder.run(subMonitor);
				if (monitor.isCanceled()) {
					result = Status.CANCEL_STATUS;
					break;
				}
			}
			monitor.done();
		}
		return result;
	}
	
}
