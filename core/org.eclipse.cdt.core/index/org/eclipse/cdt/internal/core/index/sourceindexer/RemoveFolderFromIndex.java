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

package org.eclipse.cdt.internal.core.index.sourceindexer;

import java.io.IOException;

import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class RemoveFolderFromIndex extends IndexRequest {
	IPath folderPath;
	char[][] exclusionPatterns;
	IProject project;

	public RemoveFolderFromIndex(IPath folderPath, char[][] exclusionPatterns, IProject project, SourceIndexer indexer) {
		super(project.getFullPath(), indexer);
		this.folderPath = folderPath;
		this.exclusionPatterns = exclusionPatterns;
		this.project = project;
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ false /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			IQueryResult[] results = index.queryInDocumentNames(this.folderPath.toString());
			// all file names belonging to the folder or its subfolders and that are not excluded (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32607)
			for (int i = 0, max = results == null ? 0 : results.length; i < max; i++) {
				String documentPath = results[i].getPath();
				if (this.exclusionPatterns == null || !Util.isExcluded(new Path(documentPath), this.exclusionPatterns)) {
					indexer.remove(documentPath, this.indexPath); // write lock will be acquired by the remove operation
				}
			}
		} catch (IOException e) {
			if (IndexManager.VERBOSE) {
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

