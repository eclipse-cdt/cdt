/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 ******************************************************************************/
/*
 * Created on May 30, 2003
 */
package org.eclipse.cdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;

public class AddCompilationUnitToIndex extends AddFileToIndex {
	char[] contents;

	public AddCompilationUnitToIndex(IFile resource, IPath indexedContainer, IndexManager manager) {
		super(resource, indexedContainer, manager);
	}
	protected boolean indexDocument(IIndex index) throws IOException {
		if (!initializeContents()) return false;
		index.add(new IFileDocument(resource, this.contents), new SourceIndexer(resource));
		return true;
	}
	public boolean initializeContents() {
		if (this.contents == null) {
			try {
				IPath location = resource.getLocation();
				if (location != null)
					this.contents = org.eclipse.cdt.internal.core.search.Util.getFileCharContent(location.toFile(), null);
			} catch (IOException e) {
			}
		}
		return this.contents != null;
	}
}
