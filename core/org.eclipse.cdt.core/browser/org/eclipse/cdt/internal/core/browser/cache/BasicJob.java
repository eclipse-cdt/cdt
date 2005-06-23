/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import org.eclipse.cdt.internal.core.browser.util.DelegatedProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class BasicJob extends Job {

	private Object fFamily;
	private DelegatedProgressMonitor fProgressMonitor= new DelegatedProgressMonitor();
	private Object fRunLock = new Object();
	private boolean fIsRunning = false;
	private static boolean VERBOSE = false;

	public BasicJob(String name, Object family) {
		super(name);
		fFamily = family;
		setPriority(BUILD);
		setSystem(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(IProgressMonitor)
	 */
	protected abstract IStatus runWithDelegatedProgress(IProgressMonitor monitor) throws InterruptedException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		if (fFamily != null) {
			return fFamily.equals(family);
		}
		return false;
	}

	public boolean isRunning() {
		synchronized(fRunLock) {
			return fIsRunning;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		synchronized(fRunLock) {
			fIsRunning = true;
		}
		
		fProgressMonitor.init();
		fProgressMonitor.addDelegate(monitor);

		IStatus result = Status.CANCEL_STATUS;
		try {
			if (monitor.isCanceled())
				throw new InterruptedException();
			
			result = runWithDelegatedProgress(fProgressMonitor);

			if (monitor.isCanceled())
				throw new InterruptedException();
		} catch(InterruptedException ex) {
			return Status.CANCEL_STATUS;
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} finally {
			fProgressMonitor.done();
			fProgressMonitor.removeAllDelegates();
			fProgressMonitor.init();
			
			synchronized(fRunLock) {
				fIsRunning = false;
			}
		}
		return result;
	}
	
	/**
	 * Forwards progress info to the progress monitor and
	 * blocks until the job is finished.
	 * 
	 * @param monitor the progress monitor.
	 * @throws InterruptedException
	 * 
	 * @see Job#join
	 */
	public void join(IProgressMonitor monitor) throws InterruptedException {
		if (monitor != null) {
			fProgressMonitor.addDelegate(monitor);
		}
		super.join();
	}

	/**
	 * Outputs message to console.
	 */
	protected static void trace(String msg) {
		if (VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") " + msg); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
