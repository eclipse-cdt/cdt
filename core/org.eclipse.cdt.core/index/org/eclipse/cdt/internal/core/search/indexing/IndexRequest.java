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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public abstract class IndexRequest implements IJob {
	protected boolean isCancelled = false;
	protected IPath indexPath;
	protected IndexManager manager;

	public IndexRequest(IPath indexPath, IndexManager manager) {
		this.indexPath = indexPath;
		this.manager = manager;
	}
	
	public boolean belongsTo(String projectName) {
		return projectName.equals(this.indexPath.segment(0));
	}
	
	public void cancel() {
		this.manager.jobWasCancelled(this.indexPath);
		this.isCancelled = true;
	}
	
	public boolean isReadyToRun() {
		IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexPath.segment(0));
		if ( !project.isAccessible() || !this.manager.isIndexEnabled( project ) )
			return false;
			
		// tag the index as inconsistent
		this.manager.aboutToUpdateIndex(indexPath, updatedIndexState());
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
				this.manager.saveIndex(index);
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
	}
	
	protected Integer updatedIndexState() {
		return IndexManager.UPDATING_STATE;
	}
}