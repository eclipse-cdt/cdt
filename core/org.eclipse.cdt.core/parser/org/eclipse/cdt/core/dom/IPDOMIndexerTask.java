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
/**
 * 
 */
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author dschaefer
 *
 */
public interface IPDOMIndexerTask {
	public static final String TRACE_ACTIVITY   = CCorePlugin.PLUGIN_ID + "/debug/indexer/activity";  //$NON-NLS-1$
	public static final String TRACE_STATISTICS = CCorePlugin.PLUGIN_ID + "/debug/indexer/statistics";  //$NON-NLS-1$
	public static final String TRACE_PROBLEMS   = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems";  //$NON-NLS-1$
	
	/**
	 * Run the sub job progress to the main job.
	 * 
	 * @param mainJob
	 */
	public void run(IProgressMonitor monitor);
	
	public IPDOMIndexer getIndexer();

	/**
	 * Returns the remaining subtasks. The count may increase over the time.
	 * Used by the framework to report progress.
	 * @since 4.0
	 */
	public int estimateRemainingSources();

	/**
	 * Used by the framework to report progress.
	 * @since 4.0
	 */
	public int getCompletedSourcesCount();

	/**
	 * Used by the framework to report progress.
	 * @since 4.0
	 */
	public int getCompletedHeadersCount();

	/**
	 * Returns information about the current subtask. 
	 * Used by the framework to report progress.
	 * @since 4.0
	 */
	public String getMonitorMessageDetail();
}
