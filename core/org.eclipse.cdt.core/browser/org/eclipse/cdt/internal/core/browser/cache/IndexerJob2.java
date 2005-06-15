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
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.index.IIndex;
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

public abstract class IndexerJob2 implements IIndexJob {

	protected IProject fProject;
	protected IIndex fProjectIndex = null;
	protected ICDTIndexer fSourceIndexer = null;
	
	public static final String FAMILY= "BasicTypeIndexerJob"; //$NON-NLS-1$

	public IndexerJob2(IndexManager indexManager, IProject project) {
		fProject = project;
		fSourceIndexer = indexManager.getIndexerForProject(project);
        fProjectIndex = getIndexForProject();
	}

	public boolean belongsTo(String family) {
		return family == FAMILY;
	}

	public void cancel() {
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	
	public String toString() {
		return FAMILY;
	}
	
	protected abstract boolean processIndex(IProgressMonitor progressMonitor) throws InterruptedException;

	public boolean execute(IProgressMonitor progressMonitor) {
		boolean success = false;
		try {
			if (fProjectIndex == null)
				return false;

			if (progressMonitor == null) {
				progressMonitor = new NullProgressMonitor();
			}
			if (progressMonitor.isCanceled())
				throw new OperationCanceledException();
			
			progressMonitor.beginTask("", 1); //$NON-NLS-1$

			success = prepareIndex(progressMonitor);

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
	
	private boolean prepareIndex(IProgressMonitor progressMonitor) throws InterruptedException {
		if (progressMonitor.isCanceled())
			throw new InterruptedException();

		if (fProjectIndex == null)
			return COMPLETE;
		
		if (fSourceIndexer == null)
			return FAILED;

		ReadWriteMonitor monitor = fSourceIndexer.getMonitorFor(fProjectIndex);
		
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		
		try {
			monitor.enterRead(); // ask permission to read
			/* if index has changed, commit these before querying */
			if (fProjectIndex.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					fSourceIndexer.saveIndex(fProjectIndex);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			
			if (progressMonitor.isCanceled())
				throw new InterruptedException();
			
			return processIndex(progressMonitor);
		} finally {
			monitor.exitRead(); // finished reading
		}
	}

	private IIndex getIndexForProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = fProject.getFullPath();
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
    
    protected int index2ICElement( int kind )
    {
        switch(kind) {
        case IIndex.TYPE_CLASS:
            return ICElement.C_CLASS;
        case IIndex.TYPE_STRUCT:
            return ICElement.C_STRUCT;
        case IIndex.TYPE_ENUM:
            return ICElement.C_ENUMERATION;
        case IIndex.TYPE_UNION:
            return ICElement.C_UNION;
        case IIndex.TYPE_TYPEDEF:
            return ICElement.C_TYPEDEF;
        default:
            return 0;
        }
    }
}

