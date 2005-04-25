/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.io.IOException;

import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class IndexerDependenciesJob extends IndexerJob {
	
	private ITypeCache fTypeCache;
	private ITypeSearchScope fScope;

	public IndexerDependenciesJob(IndexManager indexManager, ITypeCache typeCache, ITypeSearchScope scope) {
		super(indexManager, typeCache.getProject());
		fTypeCache = typeCache;
		fScope = scope;
	}

	protected boolean processIndex(IIndex index, IProject project, IProgressMonitor progressMonitor) throws InterruptedException {
		IndexInput input = new BlocksIndexInput(index.getIndexFile());
		try {
			input.open();
			flushDependencies(input, progressMonitor);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				return false;
			}
		}
	}

	private void flushDependencies(IndexInput input, IProgressMonitor progressMonitor)
		throws InterruptedException, IOException {
		if (progressMonitor.isCanceled())
			throw new InterruptedException();

		IEntryResult[] includeEntries = input.queryEntriesPrefixedBy(Index.encodeEntry(IIndex.INCLUDE, IIndex.ANY, IIndex.REFERENCE));
		if (includeEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < includeEntries.length; ++i) {
				if (progressMonitor.isCanceled())
					throw new InterruptedException();

				IEntryResult entry = includeEntries[i];
				IPath includePath = getIncludePath(entry);
				
				if (fScope != null && fScope.encloses(includePath)) {
					int[] references = entry.getFileReferences();
					if (references != null) {
						for (int j = 0; j < references.length; ++j) {
							if (progressMonitor.isCanceled())
								throw new InterruptedException();

							IndexedFileEntry file = input.getIndexedFile(references[j]);
							if (file != null && file.getPath() != null) {
								IPath path = PathUtil.getWorkspaceRelativePath(file.getPath());
								fTypeCache.flush(path);
							}
						}
					}
				}
			}
		}
	}

	private IPath getIncludePath(IEntryResult entry) {
		char[] word = entry.getWord();
		int firstSlash = CharOperation.indexOf(ICIndexStorageConstants.SEPARATOR, word, 0);
		String include = String.valueOf(CharOperation.subarray(word, firstSlash + 1, -1));
		return PathUtil.getWorkspaceRelativePath(include);
	}
}
