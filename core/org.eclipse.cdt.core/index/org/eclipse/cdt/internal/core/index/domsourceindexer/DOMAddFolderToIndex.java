/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class DOMAddFolderToIndex extends DOMIndexRequest {
	IPath folderPath;
	IProject project;
	char[][] exclusionPattern;
	ArrayList sourceFilesToIndex;
	ArrayList headerFilesToIndex;
	
	public DOMAddFolderToIndex(IPath folderPath, IProject project, char[][] exclusionPattern, DOMSourceIndexer indexer) {
		super(project.getFullPath(), indexer);
		this.folderPath = folderPath;
		this.project = project;
		this.exclusionPattern = exclusionPattern;
		this.sourceFilesToIndex = new ArrayList();
		this.headerFilesToIndex = new ArrayList();
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		
		if (!project.isAccessible()) return true; // nothing to do
		IResource folder = this.project.getParent().findMember(this.folderPath);
		if (folder == null || folder.getType() == IResource.FILE) return true; // nothing to do, source folder was removed

		/* ensure no concurrent write access to index */	
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read

			// final IPath container = this.indexPath;
			//final IndexManager indexManager = this.manager;
			final char[][] pattern = exclusionPattern;
			folder.accept(
				new IResourceProxyVisitor() {
					public boolean visit(IResourceProxy proxy) throws CoreException {
						switch(proxy.getType()) {
							case IResource.FILE :
								IResource resource = proxy.requestResource();
								if (CoreModel.isValidTranslationUnitName(resource.getProject(),resource.getName())) {
									if (pattern == null || !Util.isExcluded(resource, pattern))
										//indexManager.addSource((IFile)resource, container);
										sortFiles((IFile) resource);
								}
								return false;
							case IResource.FOLDER :
								if (pattern != null && Util.isExcluded(proxy.requestResource(), pattern))
									return false;
						}
						return true;
					}
				},
				IResource.NONE
			);
			scheduleJobs();
		} catch (CoreException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to add " + this.folderPath + " to index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	
	/**
	 * 
	 */
	private void scheduleJobs() {
		//Schedule the source jobs first, then the headers
		for (int i=0; i<sourceFilesToIndex.size(); i++)
			this.indexer.addSource((IFile)sourceFilesToIndex.get(i), this.indexPath, false);
		
		for (int i=0;i<headerFilesToIndex.size(); i++)
			this.indexer.addSource((IFile)headerFilesToIndex.get(i), this.indexPath, true);
	}

	public String toString() {
		return "adding " + this.folderPath + " to index " + this.indexPath; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	protected void sortFiles(IFile file){
		
		/* Check to see if this is a header file */ 
		boolean isHeader = CoreModel.isValidHeaderUnitName(file.getProject(), file.getName());
		/* See if this file has been encountered before */
		if (isHeader)
			headerFilesToIndex.add(file);

		boolean isSource = CoreModel.isValidSourceUnitName(file.getProject(), file.getName());
		if (isSource)
			sourceFilesToIndex.add(file);
		
	}
}

