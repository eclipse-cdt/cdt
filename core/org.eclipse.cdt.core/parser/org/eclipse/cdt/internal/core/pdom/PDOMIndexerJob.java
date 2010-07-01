/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX Software Systems) - initial API and implementation
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
 * Job running multiple indexer tasks.
 */
public class PDOMIndexerJob extends Job {
	/**
	 * Job updating the progress monitor of the indexer job.
	 */
	final class ProgressUpdateJob extends Job {
		private boolean fStopped= false;

		private ProgressUpdateJob() {
			super(CCorePlugin.getResourceString("PDOMIndexerJob.updateMonitorJob")); //$NON-NLS-1$
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor m) {
			int currentTick= 0;
			while(!fStopped && !m.isCanceled()) {
				currentTick= pdomManager.getMonitorMessage(PDOMIndexerJob.this, currentTick, TOTAL_MONITOR_WORK);
				try {
					Thread.sleep(PROGRESS_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
	}

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

	public synchronized void subTask(String msg) {
		if (fMonitor != null) {
			fMonitor.subTask(msg);
		}
	}

	public synchronized void worked(int i) {
		if (fMonitor != null) {
			fMonitor.worked(i);
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		fMonitor = monitor;
		String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
		monitor.beginTask(taskName, TOTAL_MONITOR_WORK);
		ProgressUpdateJob monitorJob= new ProgressUpdateJob();
		monitorJob.schedule();
		try {
			IProgressMonitor npm= new NullProgressMonitor() {
				@Override
				public boolean isCanceled() {
					synchronized(PDOMIndexerJob.this) {
						return fMonitor == null || fMonitor.isCanceled();
					}
				}
				@Override
				public void setCanceled(boolean cancelled) {
					synchronized(PDOMIndexerJob.this) {
						if (fMonitor != null) {
							fMonitor.setCanceled(cancelled);
						}
					}
				}
				@Override
				public void subTask(String name) {
					sMonitorDetail= name;
				}
			};
			do {
				synchronized(taskMutex) {
					currentTask= null;
					taskMutex.notifyAll();

					// user cancel, tell manager and return
					if (monitor.isCanceled()) {
						pdomManager.cancelledIndexerJob(cancelledByManager);
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
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					catch (OperationCanceledException e) {
					}
				}
			}
			while (currentTask != null);
			return Status.OK_STATUS;
		}
		catch (RuntimeException e) {
			CCorePlugin.log(e);
			pdomManager.cancelledIndexerJob(true);
			synchronized (taskMutex) {
				currentTask= null;
				taskMutex.notifyAll();
			}
			throw e;
		}
		catch (Error e) {
			CCorePlugin.log(e);
			pdomManager.cancelledIndexerJob(true);
			synchronized (taskMutex) {
				currentTask= null;
				taskMutex.notifyAll();
			}
			throw e;
		}
		finally {
			synchronized(this) {
				fMonitor= null;
			}
			monitorJob.cancel();
			monitor.done();
		}
	}

	private String getClassName(Object obj) {
		String name= obj.getClass().getName();
		name= name.substring(name.lastIndexOf('.')+1);
		return name;
	}
		
	public void cancelJobs(IPDOMIndexer indexer, boolean waitUntilCancelled) {
		synchronized (taskMutex) {
			if (currentTask != null && 
					(indexer == null || currentTask.getIndexer() == indexer)) {
				synchronized(this) {
					if (fMonitor != null) {
						fMonitor.setCanceled(true);
					}
				}
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
	
	@Override
	public boolean belongsTo(Object family) {
		return family == pdomManager;
	}
}
