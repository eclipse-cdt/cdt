/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public abstract class IndexRequest implements IIndexJob {
	protected boolean isCancelled = false;
	protected IPath indexPath = null;
	protected SourceIndexer indexer = null;
	
	public IndexRequest(IPath indexPath, SourceIndexer indexer) {
		this.indexPath = indexPath;
		this.indexer = indexer;
	}
	
	public boolean belongsTo(String projectName) {
		return projectName.equals(this.indexPath.segment(0));
	}
	
	public void cancel() {
		this.indexer.jobFinishedNotification( this );
		this.indexer.jobWasCancelled(this.indexPath);
		this.isCancelled = true;
	}
	
	public boolean isReadyToRun() {
		IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexPath.segment(0));
		if ( !project.isAccessible() || !this.indexer.isIndexEnabled( project ) )
			return false;
		
		// tag the index as inconsistent
		indexer.aboutToUpdateIndex(indexPath, updatedIndexState());
		return true;
	}
	/*
	 * This code is assumed to be invoked while monitor has read lock
	 */
	protected void saveIfNecessary(IIndex index, ReadWriteMonitor monitor) throws IOException {
		/* if index has changed, commit these before querying */
		if (index.hasChanged()) {
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				indexer.saveIndex(index);
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
	}
	
	protected Integer updatedIndexState() {
		return CIndexStorage.UPDATING_STATE;
	}
	
	public IPath getIndexPath(){
		return indexPath;
	}
}
