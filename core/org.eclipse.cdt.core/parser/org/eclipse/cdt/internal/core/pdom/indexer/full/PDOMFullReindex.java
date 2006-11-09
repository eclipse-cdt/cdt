/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFullReindex extends PDOMFullIndexerJob {

	private ArrayList fTUs= new ArrayList();

	public PDOMFullReindex(PDOMFullIndexer indexer) throws CoreException {
		super(indexer);
		fTotalTasks= 1;
	}

	public void run(final IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();	
			boolean allfiles= getIndexAllFiles();
			List optional= new ArrayList();
			
			collectSources(indexer.getProject(), fTUs, optional, allfiles);
			if (allfiles) {
				fTUs.addAll(optional);
				optional.clear();
			}
			
			fTotalTasks+= fTUs.size()+1;
			fCompletedTasks++;

			setupIndexAndReaderFactory();
			clearIndex(index);

			if (getRemainingSubtaskCount() == 1 || monitor.isCanceled()) {
				return;
			}

			registerTUsInReaderFactory(fTUs, optional);	
			fCompletedTasks++;
			
			parseTUs(fTUs, monitor);

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println(indexer.getID()+" indexing time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ " " + indexer.getProject().getElementName()); //$NON-NLS-1$
		} catch (CoreException e) {
			if (e.getStatus() != Status.CANCEL_STATUS)
				CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
	}
}
