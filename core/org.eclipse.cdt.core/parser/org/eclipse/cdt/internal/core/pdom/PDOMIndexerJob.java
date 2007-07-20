/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems
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
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author dschaefer
 *
 */
public class PDOMIndexerJob extends Job {
	private static final int PROGRESS_UPDATE_INTERVAL = 500;
	private static final int TOTAL_MONITOR_WORK = 1000;
	static volatile String sMonitorDetail= null;
	
	private final PDOMManager pdomManager;
	private IPDOMIndexerTask currentTask;
	private boolean cancelledByManager= false;
	private Object taskMutex = new Object();
	private IProgressMonitor fMonitor;
	private final boolean fShowActivity;
	
	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.pdomManager = manager;
		fShowActivity= PDOMIndexerTask.checkDebugOption(IPDOMIndexerTask.TRACE_ACTIVITY, "true"); //$NON-NLS-1$
		setPriority(Job.LONG);
	}

	protected IStatus run(IProgressMonitor monitor) {
		final long start= System.currentTimeMillis();
		fMonitor = monitor;
		String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
		monitor.beginTask(taskName, TOTAL_MONITOR_WORK);
		Job monitorJob= startMonitorJob(monitor);
		try {
			IProgressMonitor npm= new NullProgressMonitor() {
				public boolean isCanceled() {
					return fMonitor.isCanceled();
				}
				public void setCanceled(boolean cancelled) {
					fMonitor.setCanceled(cancelled);
				}
				public void subTask(String name) {
					sMonitorDetail= name;
				}
			};
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
						String name= null;
						long time= 0;
						if (fShowActivity) {
							name= getClassName(currentTask);
							time= -System.currentTimeMillis();
							System.out.println("Indexer: start " + name); //$NON-NLS-1$
						}
						currentTask.run(npm);
						if (fShowActivity) {
							time += System.currentTimeMillis();
							System.out.println("Indexer: completed " + name + "[" + time + "ms]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
					catch (OperationCanceledException e) {
					}
				}
			}
			while (currentTask != null);
			
			// work-around for https://bugs.eclipse.org/bugs/show_bug.cgi?id=197258
			long rest= 100-(System.currentTimeMillis()-start);
			if (rest > 0) {
				try {
					Thread.sleep(rest);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
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
			monitorJob.cancel();
			monitor.done();
		}
	}

	private String getClassName(Object obj) {
		String name= obj.getClass().getName();
		name= name.substring(name.lastIndexOf('.')+1);
		return name;
	}
		
	private Job startMonitorJob(final IProgressMonitor targetMonitor) {
		Job monitorJob= new Job(CCorePlugin.getResourceString("PDOMIndexerJob.updateMonitorJob")) {  //$NON-NLS-1$
			protected IStatus run(IProgressMonitor m) {
				int currentTick= 0;
				while(!m.isCanceled() && !targetMonitor.isCanceled()) {
					currentTick= pdomManager.getMonitorMessage(targetMonitor, currentTick, TOTAL_MONITOR_WORK);
					try {
						Thread.sleep(PROGRESS_UPDATE_INTERVAL);
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					}
				}
				return Status.OK_STATUS;
			}
		};
		monitorJob.setSystem(true);
		monitorJob.schedule();
		return monitorJob;
	}

	public void cancelJobs(IPDOMIndexer indexer, boolean waitUntilCancelled) {
		synchronized (taskMutex) {
			if (currentTask != null && 
					(indexer == null || currentTask.getIndexer() == indexer)) {
				fMonitor.setCanceled(true);
				cancelledByManager = true;
				if (waitUntilCancelled) {
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
	
	public boolean belongsTo(Object family) {
		return family == pdomManager;
	}
}
