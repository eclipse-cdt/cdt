/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.nullindexer;

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

public class NullIndexer extends AbstractCExtension implements ICDTIndexer {

    public int getIndexerFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addRequest(IProject project, IResourceDelta delta, int kind) {
		// TODO Auto-generated method stub

	}

	public void removeRequest(IProject project, IResourceDelta delta, int kind) {
		// TODO Auto-generated method stub

	}

	public void indexJobFinishedNotification(IIndexJob job) {
		// TODO Auto-generated method stub

	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void notifyIdle(long idlingTime) {
		// TODO Auto-generated method stub

	}

	public void notifyIndexerChange(IProject project) {
		// TODO Auto-generated method stub

	}

	public boolean isIndexEnabled(IProject project) {
		// TODO Auto-generated method stub
		return false;
	}

	public IIndexStorage getIndexStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	public IIndex getIndex(IPath path, boolean reuseExistingFile,
			boolean createIfMissing) {
		// TODO Auto-generated method stub
		return null;
	}

	public void indexerRemoved(IProject project) {
        CCorePlugin.getDefault().getCoreModel().getIndexManager().removeIndexerProblems(project);
	}

	public void index(IFile document, IIndexerOutput output) throws IOException {
		// TODO Auto-generated method stub

	}

	public boolean shouldIndex(IFile file) {
		// TODO Auto-generated method stub
		return false;
	}

	public void notifyListeners(IndexDelta indexDelta) {
		// TODO Auto-generated method stub
		
	}

	public void addResource(IProject project, IResource resource) {
		// TODO Auto-generated method stub
		
	}

	public void removeResource(IProject project, IResource resource) {
		// TODO Auto-generated method stub
		
	}

	public void addResourceByPath(IProject project, IPath path, int resourceType) {
		// TODO Auto-generated method stub
		
	}

    public ReadWriteMonitor getMonitorFor(IIndex index) {
        return null;
    }

    public void saveIndex(IIndex index) throws IOException {
        // TODO Auto-generated method stub
    }

    public void setIndexerProject(IProject project) {
        // TODO Auto-generated method stub
        
    }

}
