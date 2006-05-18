/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastReindex extends PDOMFastIndexerJob {

	public PDOMFastReindex(PDOMFastIndexer indexer) throws CoreException {
		super(indexer);
	}
	
	private void addSources(ICContainer container, IProgressMonitor monitor) throws CoreException {
		ITranslationUnit[] tus = container.getTranslationUnits();
		for (int i = 0; i < tus.length; ++i) {
			if (monitor.isCanceled())
				throw new CoreException(Status.CANCEL_STATUS);
			ITranslationUnit tu = tus[i];
			if (tu.isSourceUnit()) {
				try {
					addTU(tu);
				} catch (Throwable e) {
					CCorePlugin.log(e);
					if (++errorCount > MAX_ERRORS)
						throw new CoreException(Status.CANCEL_STATUS);
				}
			}
		}
		
		ICContainer[] childContainers = container.getCContainers();
		for (int i = 0; i < childContainers.length; ++i)
			addSources(childContainers[i], monitor);
	}
	
	public void run(final IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			
			ISourceRoot[] roots = indexer.getProject().getAllSourceRoots();
			for (int i = 0; i < roots.length; ++i)
				addSources(roots[i], monitor);

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID
					+ "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Fast Reindex Time: " + (System.currentTimeMillis() - start)
						+ " " + indexer.getProject().getElementName()); //$NON-NLS-1$

		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

}
