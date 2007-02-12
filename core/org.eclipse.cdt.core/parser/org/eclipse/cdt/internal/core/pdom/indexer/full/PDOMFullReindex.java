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
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFullReindex extends PDOMFullIndexerJob {

	public PDOMFullReindex(PDOMFullIndexer indexer) throws CoreException {
		super(indexer);
	}

	public void run(final IProgressMonitor monitor) {
		long start = System.currentTimeMillis();	
		try {
			boolean allFiles= getIndexAllFiles();
			List/*<ITranslationUnit>*/ sources= new ArrayList/*<ITranslationUnit>*/();
			List/*<ITranslationUnit>*/ headers= new ArrayList/*<ITranslationUnit>*/();
			
			collectSources(indexer.getProject(), sources, 
					allFiles ? headers : null, allFiles, monitor);
			fTotalSourcesEstimate= sources.size() + headers.size();

			setupIndexAndReaderFactory();
			clearIndex(index);

			if (fTotalSourcesEstimate == 0 || monitor.isCanceled()) {
				return;
			}

			registerTUsInReaderFactory(sources);
			index.acquireReadLock();
			try {
				parseTUs(sources, headers, monitor);
			}
			finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			if (e.getStatus() != Status.CANCEL_STATUS)
				CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
		traceEnd(start);
	}
}
