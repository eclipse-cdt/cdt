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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * This runnable context blocks the UI and can therefore have a shell assigned to
 * it (since the shell won't be closed by the user before the runnable completes).
 */
public class SystemProgressDialogRunnableContext implements ISystemRunnableContext {

	private Shell shell;
	private IRunnableContext runnableContext;
	private ISchedulingRule schedulingRule;
	private boolean postponeBuild;
	
	/**
	 * Constructor.
	 * @param shell the shell for the runnable context.
	 */
	public SystemProgressDialogRunnableContext(Shell shell) {
		this.shell = shell;
	}

	/**
	 * Returns whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @return <code>true</code> if the auto-build will be postponed while this
	 * context is executing a runnable, <code>false</code> otherwise.
	 */
	public boolean isPostponeBuild() {
		return postponeBuild;
	}
	
	/**
	 * Sets whether the auto-build will be postponed while this
	 * context is executing a runnable.
	 * @param postponeBuild <code>true</code> to postpone the auto-build, <code>false</code< otherwise.
	 */
	public void setPostponeBuild(boolean postponeBuild) {
		this.postponeBuild = postponeBuild;
	}
	
	/**
	 * Returns the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be obtained.
	 * @return the scheduling rule to be obtained or <code>null</code>.
	 */
	public ISchedulingRule getSchedulingRule() {
		return schedulingRule;
	}
	
	/**
	 * Sets the scheduling rule that will be obtained before the context
	 * executes a runnable or <code>null</code> if no scheduling rule is to be obtained.
	 * @param schedulingRule the scheduling rule to be obtained or <code>null</code>.
	 */
	public void setSchedulingRule(ISchedulingRule schedulingRule) {
		this.schedulingRule = schedulingRule;
	}
	
	/**
	 * Returns the shell.
	 * @see org.eclipse.rse.ui.operations.ISystemRunnableContext#getShell()
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * Sets the runnable context that is used to execute the runnable. By default,
	 * the workbench's progress service is used, but clients can provide their own.
	 * @param runnableContext the runnable contenxt used to execute runnables.
	 */
	public void setRunnableContext(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
	}
	
	/**
	 * Runs the runnable.
	 * @see org.eclipse.rse.ui.operations.ISystemRunnableContext#run(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		// fork and cancellable
		getRunnableContext().run(true, true, wrapRunnable(runnable));
	}

	/**
	 * Returns the runnable context. If a runnable context was not set, the default is to use the workbench
	 * progress service.
	 * @return the runnable context.
	 */
	private IRunnableContext getRunnableContext() {
		
		// no runnable context set, so we create our default
		if (runnableContext == null) {
			
			return new IRunnableContext() {
				
				public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
					
					// get the workbench progress service
					IProgressService manager = PlatformUI.getWorkbench().getProgressService();
					
					// run the runnable in a non-UI thread and set the cursor to busy
					manager.busyCursorWhile(runnable);
				}
			};
		}
		
		return runnableContext;
	}

	/**
	 * Wraps the runnable as required and returns the wrapper runnable. If there is no scheduling rule, and
	 * auto-builds do not have to be postponed, then the wrapper simply defers to the runnable. Otherwise,
	 * we execute the runnable as an atomic workspace operation.
	 * @param runnable the runnable to wrap.
	 * @return the wrapper runnable.
	 */
	private IRunnableWithProgress wrapRunnable(final IRunnableWithProgress runnable) {
		
		// wrap the runnable in another runnable
		return new IRunnableWithProgress() {
			
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {
					
					// if there is no scheduling rule, and if auto-build does not have to be postponed
					// then simply use the given runnable
					if (schedulingRule == null && !postponeBuild) {
						runnable.run(monitor);
					}
					// otherwise, we need to run taking into account the scheduling rule
					else {
						
						// array for holding exceptions
						final Exception[] exception = new Exception[] { null };
						
						// we run as an atomic workspace operation with a scheduling rule and allow updates
						// create a workspace runnable
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
								public void run(IProgressMonitor pm) throws CoreException {
									try {
										// just use the given runnable
										runnable.run(pm);
									}
									catch (InvocationTargetException e) {
										exception[0] = e;
									}
									catch (InterruptedException e) {
										exception[0] = e;
									}
								}
							}, schedulingRule, 0, monitor);
						
						if (exception[0] != null) {
							if (exception[0] instanceof InvocationTargetException) {
								throw (InvocationTargetException)exception[0];
							}
							else if (exception[0] instanceof InterruptedException) {
								throw (InterruptedException)exception[0];	
							}
						}
					}
				}
				catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}
}