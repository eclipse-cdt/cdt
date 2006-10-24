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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
class PDOMFastReindex extends PDOMFastIndexerJob {
	private ArrayList fTUs= new ArrayList();
	
	public PDOMFastReindex(PDOMFastIndexer indexer) throws CoreException {
		super(indexer);
		collectSources(indexer.getProject(), fTUs, fTUs);
		fFilesToIndex= fTUs.size()+1;
	}
		
	public void run(final IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			setupIndexAndReaderFactory();
			registerTUsInReaderFactory(fTUs);
			
			clearIndex(index);
			fFilesToIndex--;

			parseTUs(fTUs, monitor);

			assert fFilesToIndex == 0;
			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Fast Reindex Time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ " " + indexer.getProject().getElementName()); //$NON-NLS-1$

		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
	}
}
