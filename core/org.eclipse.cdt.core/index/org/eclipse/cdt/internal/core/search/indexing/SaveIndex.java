/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.processing.JobManager;

/*
 * Save the index of a project.
 */
public class SaveIndex extends IndexRequest {
	public SaveIndex(IPath indexPath, IndexManager manager) {
		super(indexPath, manager);
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		IIndex index = this.manager.getIndex(this.indexPath, true /*reuse index file*/, false /*don't create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = this.manager.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterWrite(); // ask permission to write
			this.manager.saveIndex(index);
		} catch (IOException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to save index " + this.indexPath + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	
	public String toString() {
		return "saving index for " + this.indexPath; //$NON-NLS-1$
	}
}
