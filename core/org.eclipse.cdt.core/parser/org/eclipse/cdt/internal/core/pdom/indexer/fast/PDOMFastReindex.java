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

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFastReindex extends PDOMFastIndexerJob {
	public PDOMFastReindex(PDOMFastIndexer indexer) throws CoreException {
		super(indexer);
	}
		
	public void run(final IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		try {
			boolean allFiles= getIndexAllFiles();
			List sources= new ArrayList();
			List headers= new ArrayList();
			collectSources(indexer.getProject(), sources, 
					allFiles ? headers : null, allFiles, monitor);
			
			if (monitor.isCanceled()) {
				return;
			}

			fTotalSourcesEstimate= sources.size() + headers.size();
			setupIndexAndReaderFactory();
			clearIndex(index);

			if (fTotalSourcesEstimate==0 || monitor.isCanceled()) {
				return;
			}
			
			index.acquireReadLock();
			try {
				registerTUsInReaderFactory(sources);
				registerTUsInReaderFactory(headers);
				parseTUs(sources, headers, monitor);
			}
			finally {
				index.releaseReadLock();
			}

		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}
}
