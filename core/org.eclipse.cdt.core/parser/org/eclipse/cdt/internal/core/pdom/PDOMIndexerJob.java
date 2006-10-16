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

	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.pdomManager = manager;
		setPriority(Job.LONG);
	}

	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long start = System.currentTimeMillis();
		
		String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		
		while (!pdomManager.finishIndexerJob()) {
			IPDOMIndexerTask nextTask= pdomManager.getNextTask();
			synchronized (taskMutex) {
				currentTask= nextTask;	// write to currentTask needs protection
			}

			try {
				currentTask.run(monitor);
			}
			catch (Exception e) {
				CCorePlugin.log(e);
			}
			boolean cancelledByUser= false;
			synchronized (taskMutex) {
				currentTask= null;		// write to currentTask needs protection
				if (cancelledByManager) {	
					// TODO chance for confusion here is user cancels
					// while project is getting deletes.
					monitor.setCanceled(false);
					cancelledByManager = false;
					taskMutex.notify();
				} 
				else {
					cancelledByUser= monitor.isCanceled();
				}
			}
			if (cancelledByUser) {
				pdomManager.cancelledByUser();
				return Status.CANCEL_STATUS;
			}
		}
		
		String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
				+ "/debug/pdomtimings"); //$NON-NLS-1$
		if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
			System.out.println("PDOM Indexer Job Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

		return Status.OK_STATUS;
	}
		
	public void cancelJobs(IPDOMIndexer indexer) {
		synchronized (taskMutex) {
			if (currentTask != null && currentTask.getIndexer().equals(indexer)) {
				monitor.setCanceled(true);
				cancelledByManager = true;
				try {
					taskMutex.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
