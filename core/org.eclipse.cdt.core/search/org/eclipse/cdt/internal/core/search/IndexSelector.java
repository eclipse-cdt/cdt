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
package org.eclipse.cdt.internal.core.search;

import java.util.ArrayList;

import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.ctagsindexer.CTagsIndexer;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Selects the indexes that correspond to projects in a given search scope
 * and that are dependent on a given focus element.
 */
public class IndexSelector {
	ICSearchScope searchScope;
	ICElement focus;
	IndexManager indexManager;
	IPath[] indexKeys; // cache of the keys for looking index up
	boolean isPolymorphicSearch;
	public IndexSelector(
		ICSearchScope searchScope,
		ICElement focus,
		boolean isPolymorphicSearch,
		IndexManager indexManager) {
		this.searchScope = searchScope;
		this.focus = focus;
		this.indexManager = indexManager;
		this.isPolymorphicSearch = isPolymorphicSearch;
	}
	/**
	 * Returns whether elements of the given project can see the given focus (an ICProject) 
	 */
	public static boolean canSeeFocus(ICElement focus, boolean isPolymorphicSearch, IPath projectPath) {
		//TODO: BOG Temp - Provide Proper Impl
		ICModel model = focus.getCModel();
		ICProject project = getCProject(projectPath, model);
		return true;
	}
	/*
	 *  Compute the list of paths which are keying index files.
	 */
	private void initializeIndexKeys() {
		
		ArrayList requiredIndexKeys = new ArrayList();
		IPath[] projects = this.searchScope.enclosingProjects();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ICElement projectFocus = this.focus == null ? null : getProject(this.focus);
		for (int i = 0; i < projects.length; i++) {
			IPath location;
			IPath path = projects[i];
			if ((!root.getProject(path.lastSegment()).exists()) // if project does not exist
				&& path.segmentCount() > 1
				&& ((location = root.getFile(path).getLocation()) == null
					|| !new java.io.File(location.toOSString()).exists()) // and internal jar file does not exist
				&& !new java.io.File(path.toOSString()).exists()) { // and external jar file does not exist
					continue;
			}
			if (projectFocus == null || canSeeFocus(projectFocus, this.isPolymorphicSearch, path)) {
				if (requiredIndexKeys.indexOf(path) == -1) {
					requiredIndexKeys.add(path);
				}
			}
		}
		this.indexKeys = new IPath[requiredIndexKeys.size()];
		requiredIndexKeys.toArray(this.indexKeys);
	}
	public IIndex[] getIndexes() {
		if (this.indexKeys == null) {
			this.initializeIndexKeys(); 
		}
		// acquire the in-memory indexes on the fly
		int length = this.indexKeys.length;
		IIndex[] indexes = new IIndex[length];
		int count = 0;
		for (int i = 0; i < length; i++){
			// may trigger some index recreation work
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ICDTIndexer indexer = indexManager.getIndexerForProject( root.getProject(indexKeys[i].toOSString()));
			
			IIndex index = null;
			
			if (indexer != null){
				if (indexer instanceof SourceIndexer){
					SourceIndexer sourceIndexer = (SourceIndexer) indexer;
					index =sourceIndexer.getIndex(indexKeys[i], true /*reuse index file*/, false /*do not create if none*/);
				}
				else if (indexer instanceof CTagsIndexer){
				    CTagsIndexer ctagsIndexer = (CTagsIndexer) indexer;
					index =ctagsIndexer.getIndex(indexKeys[i], true /*reuse index file*/, false /*do not create if none*/);
				}
			}
			if (index != null) indexes[count++] = index; // only consider indexes which are ready yet
		}
		if (count != length) {
			System.arraycopy(indexes, 0, indexes=new IIndex[count], 0, count);
		}
		return indexes;
	}
	/**
	 * Returns the project that corresponds to the given path.
	 * Returns null if the path doesn't correspond to a project.
	 */
	private static ICProject getCProject(IPath path, ICModel model) {
		ICProject project = model.getCProject(path.lastSegment());
		if (project.exists()) {
			return project;
		} else {
			return null;
		}
	}
	public static ICElement getProject(ICElement element) {
		while (!(element instanceof ICProject)) {
			element = element.getParent();
		}
		return element;
	}
}
