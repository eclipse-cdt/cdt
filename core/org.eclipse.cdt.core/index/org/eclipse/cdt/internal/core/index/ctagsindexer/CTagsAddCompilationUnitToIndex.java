/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsAddCompilationUnitToIndex extends CTagsAddFileToIndex {
    char[] contents;

	public CTagsAddCompilationUnitToIndex(IFile resource, IPath indexedContainer, CTagsIndexer indexer) {
		super(resource, indexedContainer, indexer);
	}
	protected boolean indexDocument(IIndex index) throws IOException {
		if (!initializeContents()) return false;
		index.add(new IFileDocument(resource, this.contents), new CTagsIndexerRunner(resource, indexer));
		
		return true;
	}
	public boolean initializeContents() {
		if (this.contents == null) {
			try {
				IPath location = resource.getLocation();
				if (location != null)
					this.contents = org.eclipse.cdt.internal.core.Util.getFileCharContent(location.toFile(), null);
			} catch (IOException e) {
			}
		}
		return this.contents != null;
	}
}
