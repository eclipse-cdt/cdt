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
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
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

		IEntryResult[] namespaceEntries = input.queryEntriesPrefixedBy(IIndexConstants.NAMESPACE_DECL);
		if (namespaceEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < namespaceEntries.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				IEntryResult entry = namespaceEntries[i];
				char[] word = entry.getWord();
				int firstSlash = CharOperation.indexOf(IIndexConstants.SEPARATOR, word, 0);
				int slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, word, firstSlash + 1);
				String name = String.valueOf(CharOperation.subarray(word, firstSlash + 1, slash));
				if (name.length() != 0) {
					String[] enclosingNames = getEnclosingNames(word, slash);
					addType(input, project, entry, ICElement.C_NAMESPACE, name, enclosingNames, monitor);
				}
			}
		}
	}
	
	private void updateTypes(IndexInput input, IProject project, IProgressMonitor monitor)
		throws InterruptedException, IOException {
		if (monitor.isCanceled())
			throw new InterruptedException();

		IEntryResult[] typeEntries = input.queryEntriesPrefixedBy(IIndexConstants.TYPE_DECL);
		if (typeEntries != null) {
			//TODO subprogress monitor
			for (int i = 0; i < typeEntries.length; ++i) {
				if (monitor.isCanceled())
					throw new InterruptedException();
				
				IEntryResult entry = typeEntries[i];
				char[] word = entry.getWord();
				int firstSlash = CharOperation.indexOf(IIndexConstants.SEPARATOR, word, 0);
				char decodedType = word[firstSlash + 1];
				int type = getElementType(decodedType);
				if (type != 0) {
					firstSlash += 2;
					int slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, word, firstSlash + 1);
					String name = String.valueOf(CharOperation.subarray(word, firstSlash + 1, slash));
					if (name.length() != 0) {  // skip anonymous structs
						String[] enclosingNames = getEnclosingNames(word, slash);
						addType(input, project, entry, type, name, enclosingNames, monitor);
					}
				} else if (decodedType == IIndexConstants.DERIVED_SUFFIX) {
					firstSlash += 2;
					int slash = CharOperation.indexOf(IIndexConstants.SEPARATOR, word, firstSlash + 1);
					String name = String.valueOf(CharOperation.subarray(word, firstSlash + 1, slash));
					if (name.length() != 0) {  // skip anonymous structs
						String[] enclosingNames = getEnclosingNames(word, slash);
						addSuperTypeReference(input, project, entry, name, enclosingNames, monitor);
					}
				}
			}
		}
	}

	private int getElementType(char decodedType) {
		switch (decodedType) {
			case IIndexConstants.CLASS_SUFFIX :
				return ICElement.C_CLASS;
			case IIndexConstants.STRUCT_SUFFIX :
				return ICElement.C_STRUCT;
			case IIndexConstants.TYPEDEF_SUFFIX :
				return ICElement.C_TYPEDEF;
			case IIndexConstants.ENUM_SUFFIX :
				return ICElement.C_ENUMERATION;
			case IIndexConstants.UNION_SUFFIX :
				return ICElement.C_UNION;
		}
		return 0;
	}

	private String[] getEnclosingNames(char[] word, int slash) {
		String[] enclosingNames= null;
		if (slash != -1 && slash + 1 < word.length) {
			char[][] temp = CharOperation.splitOn('/', CharOperation.subarray(word, slash + 1, -1));
			enclosingNames= new String[temp.length];
			for (int i = 0; i < temp.length; i++) {
				enclosingNames[i] = String.valueOf(temp[temp.length - i - 1]);
			}
		}
		return enclosingNames;
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
				IndexedFile file = input.getIndexedFile(references[0]);
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

				IndexedFile file = input.getIndexedFile(references[i]);
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
