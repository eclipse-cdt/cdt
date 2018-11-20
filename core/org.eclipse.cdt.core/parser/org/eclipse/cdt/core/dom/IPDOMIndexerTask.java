/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPDOMIndexerTask {
	public static final String TRACE_ACTIVITY = CCorePlugin.PLUGIN_ID + "/debug/indexer/activity"; //$NON-NLS-1$
	public static final String TRACE_STATISTICS = CCorePlugin.PLUGIN_ID + "/debug/indexer/statistics"; //$NON-NLS-1$
	public static final String TRACE_INCLUSION_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/inclusion"; //$NON-NLS-1$
	public static final String TRACE_SCANNER_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/scanner"; //$NON-NLS-1$
	public static final String TRACE_SYNTAX_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems/syntax"; //$NON-NLS-1$
	public static final String TRACE_PROBLEMS = CCorePlugin.PLUGIN_ID + "/debug/indexer/problems"; //$NON-NLS-1$

	/**
	 * Called by the framework to perform the task.
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException;

	/**
	 * Notifies the task that it should stop executing at its earliest convenience.
	 * It's up to the task whether to react to this method or not.
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This default method is not intended to be re-implemented or extended by clients.
	 */
	public default void cancel() {
	}

	/**
	 * Returns the indexer the task belongs to.
	 */
	public IPDOMIndexer getIndexer();

	/**
	 * Returns progress information for the task.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IndexerProgress getProgressInformation();

	/**
	 * Takes files from another task and adds them to this task in front of all not yet processed
	 * files. The urgent work my be rejected if this task is not capable of accepting it, or if
	 * the amount of urgent work is too large compared to the work already assigned to this task.
	 * @param task the task to add the work from.
	 * @return {@code true} if the work was accepted, {@code false} if it was rejected.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=319330"
	 * @since 5.3
	 */
	public boolean acceptUrgentTask(IPDOMIndexerTask task);
}
