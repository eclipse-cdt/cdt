/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.IOException;

import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IJob;
import org.eclipse.core.runtime.IPath;

public abstract class DependencyRequest implements IJob {
	protected boolean isCancelled = false;
	protected DependencyManager manager;
	protected IPath dependencyTreePath;
		
	public DependencyRequest(IPath path, DependencyManager manager) {
		this.dependencyTreePath = path;
		this.manager = manager;
	}

	public DependencyRequest(DependencyManager manager) {
			this.manager = manager;
	}
	
	public boolean belongsTo(String projectName) {
		return projectName.equals(this.dependencyTreePath.segment(0));
	}
	
	public void cancel() {
		this.manager.jobWasCancelled(this.dependencyTreePath);
		this.isCancelled = true;
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	/*
	 * This code is assumed to be invoked while monitor has read lock
	 */
	protected void saveIfNecessary(IDependencyTree tree, ReadWriteMonitor monitor) throws IOException {
		/* if tree has changed, commit these before querying */
		if (tree.hasChanged()) {
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				//this.manager.saveTree(tree);
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
	}
	
	protected Integer updatedIndexState() {
		return DependencyManager.UPDATING_STATE;
	}
}
