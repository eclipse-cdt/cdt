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

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.cdt.core.browser.TypeReference;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class IndexerTypesJob extends IndexerJob {
	
	private ITypeCache fTypeCache;

	public IndexerTypesJob(IndexManager indexManager, ITypeCache typeCache, ITypeSearchScope scope) {
		super(indexManager, typeCache.getProject());
		fTypeCache = typeCache;
	}

	protected boolean processIndex(IIndex index, IProject project, IProgressMonitor progressMonitor) throws InterruptedException {
		IndexInput input = new BlocksIndexInput(index.getIndexFile());
		try {
			input.open();
			updateNamespaces(input, project, progressMonitor);
			updateTypes(input, project, progressMonitor);
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

	private void updateNamespaces(IndexInput input, IProject project, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		if (monitor.isCanceled())
			throw new InterruptedException();

		IEntryResult[] namespaceEntries = input.queryEntriesPrefixedBy(Index.encodeEntry(IIndex.NAMESPACE, IIndex.ANY, IIndex.DECLARATION));
		if (namespaceEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < namespaceEntries.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				IEntryResult entry = namespaceEntries[i];
				String name = entry.getName();
				if (name.length() != 0) {
					String[] enclosingNames = entry.getEnclosingNames();
					addType(input, project, entry, ICElement.C_NAMESPACE, name, enclosingNames, monitor);
				}
			}
		}
	}
	
	private void updateTypes(IndexInput input, IProject project, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		if (monitor.isCanceled())
			throw new InterruptedException();

		IEntryResult[] typeEntries = input.queryEntriesPrefixedBy(Index.encodeEntry(IIndex.TYPE, IIndex.ANY, IIndex.DECLARATION));
		if (typeEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < typeEntries.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				IEntryResult entry = typeEntries[i];
				
				String name = entry.getName();
				switch (entry.getKind() ) {
				case IIndex.TYPE_CLASS :
				case IIndex.TYPE_STRUCT :
				case IIndex.TYPE_TYPEDEF :
				case IIndex.TYPE_ENUM :
				case IIndex.TYPE_UNION :			
					if (name.length() != 0) {  // skip anonymous structs
						addType(input, project, entry, entry.getKind(), name, entry.getEnclosingNames(), monitor);
					}
					break;
				case IIndex.TYPE_DERIVED :
					if (name.length() != 0) {  // skip anonymous structs
						addSuperTypeReference(input, project, entry, name, entry.getEnclosingNames(), monitor);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	private void addType(IndexInput input, IProject project, IEntryResult entry, int type, String name, String[] enclosingNames, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		QualifiedTypeName qualifiedName = new QualifiedTypeName(name, enclosingNames);
		ITypeInfo info = fTypeCache.getType(type, qualifiedName);
		if (info == null || info.isUndefinedType()) {
			int[] references = entry.getFileReferences();
			if (references != null && references.length > 0) {
				// add new type to cache
				if (info != null) {
					info.setCElementType(type);
				} else {
					info = new TypeInfo(type, qualifiedName);
					fTypeCache.insert(info);
				}

//				for (int i = 0; i < references.length; ++i) {
//					if (monitor.isCanceled())
//						throw new InterruptedException();
//
//					IndexedFile file = input.getIndexedFile(references[i]);
//					if (file != null && file.getPath() != null) {
//						IPath path = PathUtil.getWorkspaceRelativePath(file.getPath());
//						info.addReference(new TypeReference(path, project));
//					}
//				}
				// just grab the first reference
				IndexedFileEntry file = input.getIndexedFile(references[0]);
				if (file != null && file.getPath() != null) {
					IPath path = PathUtil.getWorkspaceRelativePath(file.getPath());
					info.addReference(new TypeReference(path, project));
				}
			}
		}
	}

	private void addSuperTypeReference(IndexInput input, IProject project, IEntryResult entry, String name, String[] enclosingNames, IProgressMonitor monitor) throws InterruptedException, IOException {
		QualifiedTypeName qualifiedName = new QualifiedTypeName(name, enclosingNames);
		ITypeInfo info = fTypeCache.getType(ICElement.C_CLASS, qualifiedName);
		if (info == null)
			info = fTypeCache.getType(ICElement.C_STRUCT, qualifiedName);
		if (info == null) {
			// add dummy type to cache
			info = new TypeInfo(0, qualifiedName);
			fTypeCache.insert(info);
		}
		int[] references = entry.getFileReferences();
		if (references != null && references.length > 0) {
			for (int i = 0; i < references.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();

				IndexedFileEntry file = input.getIndexedFile(references[i]);
				if (file != null && file.getPath() != null) {
					IPath path = PathUtil.getWorkspaceRelativePath(file.getPath());
					info.addDerivedReference(new TypeReference(path, project));
//
//					// get absolute path
//					IPath path = new Path(file.getPath());
//					IPath projectPath = project.getFullPath();
//					if (projectPath.isPrefixOf(path)) {
//						path = project.getLocation().append(path.removeFirstSegments(projectPath.segmentCount()));
//					}
//					info.addDerivedReference(new TypeReference(path, project));
				}
			}
		}
	}
}
