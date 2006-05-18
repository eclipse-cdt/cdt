/**
 * 
 */
package org.eclipse.cdt.internal.core.pdom;

import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
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
	
	public PDOMIndexerJob(PDOMManager manager) {
		super(CCorePlugin.getResourceString("pdom.indexer.name")); //$NON-NLS-1$
		this.manager = manager;
		setPriority(Job.LONG);
	}

	protected IStatus run(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		
		String taskName = CCorePlugin.getResourceString("pdom.indexer.task"); //$NON-NLS-1$
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		
		fillQueue();
		while (true) {
			while (!queue.isEmpty()) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				IPDOMIndexerTask task = (IPDOMIndexerTask)queue.removeFirst();
				task.run(monitor);
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
		IPDOMIndexerTask task = manager.getNextTask();
		while (task != null) {
			queue.addLast(task);
			task = manager.getNextTask();
		}
	}
}
