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

import java.io.IOException;

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.sourceindexer.AddCompilationUnitToIndex;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class DOMAddCompilationUnitToIndex extends AddCompilationUnitToIndex {

    public DOMAddCompilationUnitToIndex(IFile resource, IPath indexedContainer,
            SourceIndexer indexer, boolean checkEncounteredHeaders) {
        super(resource, indexedContainer, indexer, checkEncounteredHeaders);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AddCompilationUnitToIndex#indexDocument(org.eclipse.cdt.internal.core.index.IIndex)
     */
    protected boolean indexDocument(IIndex index) throws IOException {
        if (!initializeContents()) return false;
        index.add(resource, new DOMSourceIndexerRunner(resource, indexer));
        
        return true;
    }

}
