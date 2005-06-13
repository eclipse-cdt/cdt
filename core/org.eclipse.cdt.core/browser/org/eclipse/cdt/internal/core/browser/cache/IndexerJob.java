/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     QNX Software Systems - adapted for type cache
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.io.IOException;

import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.domsourceindexer.DOMSourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public abstract class IndexerJob implements IIndexJob {

	private IndexManager fIndexManager;
	private IProject fProject;
	private IIndex fProjectIndex = null;
	private DOMSourceIndexer fSourceIndexer = null;
	
	public static final String FAMILY= "BasicTypeIndexerJob"; //$NON-NLS-1$

	public IndexerJob(IndexManager indexManager, IProject project) {
		fIndexManager = indexManager;
		fProject = project;
		//Get the indexer assigned to this project; check to see if it's 
		//a Source Indexder
		ICDTIndexer indexer = indexManager.getIndexerForProject(project);
		if (indexer instanceof DOMSourceIndexer)
			fSourceIndexer = (DOMSourceIndexer) indexer;
	}

	public boolean belongsTo(String family) {
		return family == FAMILY;
	}

	public void cancel() {
	}
	
	public boolean isReadyToRun() {
		if (fProjectIndex == null) { // only check once. As long as this job is used, it will keep the same index picture
			getIndexForProject(fProject); // will only cache answer if all indexes were available originally
		}
		return true;
	}
	
	public String toString() {
		return FAMILY;
	}
	
	protected abstract boolean processIndex(IIndex index, IProject project, IProgressMonitor progressMonitor) throws InterruptedException;

	public boolean execute(IProgressMonitor progressMonitor) {
		boolean success = false;
		try {
			fProjectIndex = getIndexForProject(fProject);
			if (fProjectIndex == null)
				return false;

			if (progressMonitor == null) {
				progressMonitor = new NullProgressMonitor();
			}
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			
			progressMonitor.beginTask("", 1); //$NON-NLS-1$

			success = prepareIndex(fProjectIndex, fProject, progressMonitor);

			if (progressMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			progressMonitor.worked(1);

			return success;
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		} finally {
			progressMonitor.done();
		}
	}
	
	private boolean prepareIndex(IIndex index, IProject project, IProgressMonitor progressMonitor) throws InterruptedException {
		if (progressMonitor.isCanceled())
			throw new InterruptedException();

		if (index == null)
			return COMPLETE;
		
		if (fSourceIndexer == null)
			return FAILED;

		ReadWriteMonitor monitor = fSourceIndexer.getMonitorFor(index);
		
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		
		try {
			monitor.enterRead(); // ask permission to read
			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					fSourceIndexer.saveIndex(index);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			
			if (progressMonitor.isCanceled())
				throw new InterruptedException();
			
			return processIndex(index, project, progressMonitor);
		} finally {
			monitor.exitRead(); // finished reading
		}
	}

	private IIndex getIndexForProject(IProject project) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = project.getFullPath();
		IPath location;
		if ((!root.getProject(path.lastSegment()).exists()) // if project does not exist
				&& path.segmentCount() > 1
				&& ((location = root.getFile(path).getLocation()) == null
						|| !new java.io.File(location.toOSString()).exists()) // and internal jar file does not exist
						&& !new java.io.File(path.toOSString()).exists()) { // and external jar file does not exist
			return null;
		}
		
		// may trigger some index recreation work
		if (fSourceIndexer != null)
			return fSourceIndexer.getIndex(path, true /*reuse index file*/, false /*do not create if none*/);
		
		return null;
	}
}

