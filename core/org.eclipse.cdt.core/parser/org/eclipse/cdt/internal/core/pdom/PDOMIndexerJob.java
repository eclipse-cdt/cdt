/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author dschaefer
 *
 */
public class PDOMIndexerJob extends Job {

	private final PDOMManager pdomManager;
	private IPDOMIndexerTask currentTask;
	private boolean cancelledByManager= false;
	private Object taskMutex = new Object();
	
	private IProgressMonitor monitor;

	private Job fMonitorJob;

	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.pdomManager = manager;
		setPriority(Job.LONG);
	}

	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;
		long start = System.currentTimeMillis();

		startMonitorJob(monitor);
		try {
			do {
				synchronized(taskMutex) {
					currentTask= null;
					taskMutex.notify();

					// user cancel, tell manager and return
					if (monitor.isCanceled()) {
						pdomManager.cancelledJob(cancelledByManager);
						return Status.CANCEL_STATUS;
					}

					// pick up new task
					currentTask= pdomManager.getNextTask();
				}

				if (currentTask != null) {
					try {
						currentTask.run(monitor);
					}
					catch (Exception e) {
						CCorePlugin.log(e);
					}					
				}
			}
			while (currentTask != null);

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Indexer Job Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

			return Status.OK_STATUS;
		}
		catch (RuntimeException e) {
			CCorePlugin.log(e);
			pdomManager.cancelledJob(true);
			synchronized (taskMutex) {
				currentTask= null;
				taskMutex.notify();
			}
			throw e;
		}
		catch (Error e) {
			CCorePlugin.log(e);
			pdomManager.cancelledJob(true);
			synchronized (taskMutex) {
				currentTask= null;
				taskMutex.notify();
			}
			throw e;
		}
		finally {
			stopMonitorJob();
		}
	}
		
	private void stopMonitorJob() {
		if (fMonitorJob != null) {
			fMonitorJob.cancel();
		}
	}

	private void startMonitorJob(final IProgressMonitor monitor) {
		fMonitorJob= new Job(CCorePlugin.getResourceString("PDOMIndexerJob.updateMonitorJob")) {  //$NON-NLS-1$
			protected IStatus run(IProgressMonitor m) {
				String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
				monitor.beginTask(taskName, 1000);
				int currentTick= 0;
				while(!m.isCanceled()) {
					currentTick= pdomManager.getMonitorMessage(monitor, currentTick, 1000);
					try {
						Thread.sleep(350);
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		};
		fMonitorJob.setSystem(true);
		fMonitorJob.schedule();
	}

	public void cancelJobs(IPDOMIndexer indexer) {
		synchronized (taskMutex) {
			if (currentTask != null && currentTask.getIndexer() == indexer) {
				monitor.setCanceled(true);
				cancelledByManager = true;
				while (currentTask != null && currentTask.getIndexer() == indexer) {
					try {
						taskMutex.wait();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}
}
