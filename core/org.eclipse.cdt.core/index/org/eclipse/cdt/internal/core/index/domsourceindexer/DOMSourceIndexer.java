/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.internal.core.index.sourceindexer.CIndexStorage;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class DOMSourceIndexer extends SourceIndexer {

    public DOMSourceIndexer() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer#addSource(org.eclipse.core.resources.IFile, org.eclipse.core.runtime.IPath, boolean)
     */
    public void addSource(IFile resource, IPath indexedContainers, boolean checkEncounteredHeaders) {
        IProject project = resource.getProject();
        
        boolean indexEnabled = false;
        if (project != null)
            indexEnabled = isIndexEnabled(project);
        else
            org.eclipse.cdt.internal.core.model.Util.log(null, "IndexManager addSource: File has no project associated : " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ 
            
        if (CCorePlugin.getDefault() == null) return;   
        
        if (indexEnabled){
            DOMAddCompilationUnitToIndex job = new DOMAddCompilationUnitToIndex(resource, indexedContainers, this, checkEncounteredHeaders);

            //If we are in WAITING mode, we need to kick ourselves into enablement
            if (!jobSet.add(resource.getLocation()) &&
                indexManager.enabledState()==IndexManager.ENABLED)
                return;
            
            
            if (indexManager.awaitingJobsCount() < CIndexStorage.MAX_FILES_IN_MEMORY) {
                // reduces the chance that the file is open later on, preventing it from being deleted
                if (!job.initializeContents()) return;
            }
            
            this.indexManager.request(job);
        }
    }

}
