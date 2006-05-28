/**
 * 
 */
package org.eclipse.cdt.internal.core.pdom;

import java.util.Iterator;
import java.util.LinkedList;

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

	private final PDOMManager manager;
	
	private LinkedList queue = new LinkedList();
	private IPDOMIndexerTask currentTask;
	private boolean isCancelling = false;
	private Object taskMutex = new Object();
	
	private IProgressMonitor monitor;

	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.manager = manager;
		setPriority(Job.LONG);
	}

	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long start = System.currentTimeMillis();
		
		String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		
		fillQueue();
		while (true) {
			while (!queue.isEmpty()) {
				synchronized (taskMutex) {
					currentTask = (IPDOMIndexerTask)queue.removeFirst();
				}
				currentTask.run(monitor);
				synchronized (taskMutex) {
					if (isCancelling) {
						// TODO chance for confusion here is user cancels
						// while project is getting deletes.
						monitor.setCanceled(false);
						isCancelling = false;
						taskMutex.notify();
					} else if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
				}
			}
			if (manager.finishIndexerJob())
				break;
			else
				fillQueue();
		}
		
		String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
				+ "/debug/pdomtimings"); //$NON-NLS-1$
		if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
			System.out.println("PDOM Indexer Job Time: " + (System.currentTimeMillis() - start));

		return Status.OK_STATUS;
	}
	
	private void fillQueue() {
		synchronized (taskMutex) {
			IPDOMIndexerTask task = manager.getNextTask();
			while (task != null) {
				queue.addLast(task);
				task = manager.getNextTask();
			}
		}
	}
	
	public void cancelJobs(IPDOMIndexer indexer) {
		synchronized (taskMutex) {
			for (Iterator i = queue.iterator(); i.hasNext();) {
				IPDOMIndexerTask task = (IPDOMIndexerTask)i.next();
				if (task.getIndexer().equals(indexer))
					i.remove();
			}
			if (currentTask != null && currentTask.getIndexer().equals(indexer)) {
				monitor.setCanceled(true);
				isCancelling = true;
				try {
					taskMutex.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
