/*******************************************************************************
 * Copyright (c) 2005, 2014 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		private boolean fCancelled;

		private ProgressUpdateJob() {
			super(CCorePlugin.getResourceString("PDOMIndexerJob.updateMonitorJob")); //$NON-NLS-1$
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor m) {
			int currentTick = 0;
			while (!fCancelled && !m.isCanceled()) {
				currentTick = pdomManager.getMonitorMessage(PDOMIndexerJob.this, currentTick, TOTAL_MONITOR_WORK);
				try {
					synchronized (this) {
						if (fCancelled)
							break;
						wait(PROGRESS_UPDATE_INTERVAL);
					}
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}

		@Override
		protected void canceling() {
			// Speed up cancellation by notifying the waiting thread.
			synchronized (this) {
				fCancelled = true;
				notify();
			}
			synchronized (taskMutex) {
				if (currentTask != null)
					currentTask.cancel();
			}
		}
	}

	private static final int PROGRESS_UPDATE_INTERVAL = 500;
	private static final int TOTAL_MONITOR_WORK = 1000;
	static volatile String sMonitorDetail = null;

	private final PDOMManager pdomManager;
	private IPDOMIndexerTask currentTask;
	private boolean cancelledByManager = false;
	private final Object taskMutex = new Object();
	private IProgressMonitor fMonitor;
	private final boolean fShowActivity;

	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.pdomManager = manager;
		fShowActivity = PDOMIndexerTask.checkDebugOption(IPDOMIndexerTask.TRACE_ACTIVITY, "true"); //$NON-NLS-1$
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
		ProgressUpdateJob monitorJob = new ProgressUpdateJob();
		monitorJob.schedule();
		try {
			IProgressMonitor npm = new NullProgressMonitor() {
				@Override
				public boolean isCanceled() {
					synchronized (PDOMIndexerJob.this) {
						return fMonitor == null || fMonitor.isCanceled();
					}
				}

				@Override
				public void setCanceled(boolean cancelled) {
					synchronized (PDOMIndexerJob.this) {
						if (fMonitor != null) {
							fMonitor.setCanceled(cancelled);
						}
					}
				}

				@Override
				public void subTask(String name) {
					sMonitorDetail = name;
				}
			};

			do {
				synchronized (taskMutex) {
					currentTask = null;
					taskMutex.notifyAll();

					// User cancel, tell manager and return.
					if (monitor.isCanceled()) {
						pdomManager.indexerJobCanceled(cancelledByManager);
						return Status.CANCEL_STATUS;
					}

					// Pick up new task.
					currentTask = pdomManager.getNextTask();
				}

				if (currentTask != null) {
					try {
						String name = null;
						long time = 0;
						if (fShowActivity) {
							name = getClassName(currentTask);
							time = -System.currentTimeMillis();
							System.out.println("Indexer: start " + name); //$NON-NLS-1$
						}
						currentTask.run(npm);
						if (fShowActivity) {
							time += System.currentTimeMillis();
							System.out.println("Indexer: completed " + name + "[" + time + "ms]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			} while (currentTask != null);
			return Status.OK_STATUS;
		} catch (OperationCanceledException e) {
			indexingAborted();
			throw e;
		} catch (RuntimeException | Error e) {
			CCorePlugin.log(e);
			indexingAborted();
			throw e;
		} finally {
			synchronized (this) {
				fMonitor = null;
			}
			monitorJob.cancel();
		}
	}

	private void indexingAborted() {
		pdomManager.indexerJobCanceled(true);
		synchronized (taskMutex) {
			currentTask = null;
			taskMutex.notifyAll();
		}
	}

	private String getClassName(Object obj) {
		String name = obj.getClass().getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name;
	}

	public void cancelJobs(IPDOMIndexer indexer, boolean waitUntilCancelled) {
		synchronized (taskMutex) {
			if (currentTask != null && (indexer == null || currentTask.getIndexer() == indexer)) {
				synchronized (this) {
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
