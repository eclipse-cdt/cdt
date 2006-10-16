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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

class PDOMFastHandleDelta extends PDOMFastIndexerJob {

	private List changed = new ArrayList();
	private List removed = new ArrayList();
	private volatile int fFilesToIndex= 0;

	public PDOMFastHandleDelta(PDOMFastIndexer indexer, ICElementDelta delta) throws CoreException {
		super(indexer);
		processDelta(delta, changed, changed, removed);
		fFilesToIndex= changed.size() + removed.size();
	}

	public void run(IProgressMonitor monitor) {
		try {
			long start = System.currentTimeMillis();
			Iterator i = changed.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				ITranslationUnit tu = (ITranslationUnit)i.next();
				changeTU(tu);
				fFilesToIndex--;
			}
						
			i = removed.iterator();
			while (i.hasNext()) {
				if (monitor.isCanceled())
					return;
				ITranslationUnit tu = (ITranslationUnit)i.next();
				removeTU(index, tu);
				fFilesToIndex--;
			}
			
			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings != null && showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
				System.out.println("PDOM Fast Delta Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$

		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (InterruptedException e) {
		}
	}
	
	public int getFilesToIndexCount() {
		return fFilesToIndex;
	}
}
