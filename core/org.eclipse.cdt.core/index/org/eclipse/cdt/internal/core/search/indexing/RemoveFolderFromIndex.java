/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 ******************************************************************************/
/*
 * Created on Jun 1, 2003
 */
package org.eclipse.cdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.cdt.internal.core.search.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.search.processing.JobManager;

class RemoveFolderFromIndex extends IndexRequest {
	IPath folderPath;
	char[][] exclusionPatterns;
	IProject project;

	public RemoveFolderFromIndex(IPath folderPath, char[][] exclusionPatterns, IProject project, IndexManager manager) {
		super(project.getFullPath(), manager);
		this.folderPath = folderPath;
		this.exclusionPatterns = exclusionPatterns;
		this.project = project;
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		IIndex index = manager.getIndex(this.indexPath, true, /*reuse index file*/ false /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			IQueryResult[] results = index.queryInDocumentNames(this.folderPath.toString());
			// all file names belonging to the folder or its subfolders and that are not excluded (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32607)
			for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
				String documentPath = results[i].getPath();
				if (this.exclusionPatterns == null || !Util.isExcluded(new Path(documentPath), this.exclusionPatterns)) {
					manager.remove(documentPath, this.indexPath); // write lock will be acquired by the remove operation
				}
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to remove " + this.folderPath + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	
	public String toString() {
		return "removing " + this.folderPath + " from index " + this.indexPath; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

