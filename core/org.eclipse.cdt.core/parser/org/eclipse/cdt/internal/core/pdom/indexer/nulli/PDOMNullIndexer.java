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

package org.eclipse.cdt.internal.core.pdom.indexer.nulli;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Doug Schaefer
 *
 * The Null Indexer which does nothing.
 */
public class PDOMNullIndexer implements IPDOMIndexer {

	public static final String ID = IPDOMManager.ID_NO_INDEXER;
	
	private ICProject project;
	
	public ICProject getProject() {
		return project;
	}
	
	public void setProject(ICProject project) {
		this.project = project;
	}
	
	public void handleDelta(ICElementDelta delta) {
	}
		
	private class PDOMNullReindex implements IPDOMIndexerTask {
		public IPDOMIndexer getIndexer() {
			return PDOMNullIndexer.this;
		}

		public void run(IProgressMonitor monitor) {
			try {
				IWritableIndex index= ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(project);
				index.acquireWriteLock(0);
				try {
					index.clear();
				}
				finally {
					index.releaseWriteLock(0);
				}
			}
			catch (InterruptedException e) {
			} 
			catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
	public void reindex() throws CoreException {
		CCorePlugin.getPDOMManager().enqueue(new PDOMNullReindex());
	}

}
