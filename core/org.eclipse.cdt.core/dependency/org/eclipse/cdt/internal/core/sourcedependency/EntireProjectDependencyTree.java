/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.impl.IFileDocument;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
/**
 * @author bgheorgh
 */
public class EntireProjectDependencyTree extends DependencyRequest {
	IProject project;
	
	public EntireProjectDependencyTree(IProject project, DependencyManager manager) {
		super(project.getFullPath(), manager);
		this.project = project;
	}

	public boolean equals(Object o) {
		if (o instanceof EntireProjectDependencyTree)
			return this.project.equals(((EntireProjectDependencyTree) o).project);
		return false;
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		if (!project.isAccessible()) return true; // nothing to do

		IDependencyTree dTree = this.manager.getDependencyTree(this.dependencyTreePath, true, /*reuse index file*/ true /*create if none*/);
		if (dTree == null) return true;
		ReadWriteMonitor monitor = this.manager.getMonitorFor(dTree);
		if (monitor == null) return true; // tree got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read
			saveIfNecessary(dTree, monitor);

			IQueryResult[] results = dTree.queryInDocumentNames(""); // get all file names already stored in this project //$NON-NLS-1$
			int max = results == null ? 0 : results.length;
			final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			for (int i = 0; i < max; i++)
				indexedFileNames.put(results[i].getPath(), DELETED);
			final long indexLastModified = max == 0 ? 0L : dTree.getIndexFile().lastModified();

			IPath cProjectPath = project.getFullPath();
		
			IWorkspaceRoot root = this.project.getWorkspace().getRoot();
			IResource sourceFolder = root.findMember(cProjectPath);
		
			if (this.isCancelled) return false;

			if (sourceFolder != null) {
					
				//collect output locations if source is project (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32041)
				final HashSet outputs = new HashSet();
		
				final boolean hasOutputs = !outputs.isEmpty();
					
				final char[][] patterns = null;
				if (max == 0) {
					sourceFolder.accept(
							new IResourceProxyVisitor() {
								public boolean visit(IResourceProxy proxy) {
									if (isCancelled) return false;
									switch(proxy.getType()) {
										case IResource.FILE :
//											TODO: BOG Put the file name checking back
										//if (Util.isCCFileName(proxy.getName())) {
											IResource resource = proxy.requestResource();
											if (resource.getLocation() != null && (patterns == null || !Util.isExcluded(resource, patterns))) {
												String name = new IFileDocument((IFile) resource).getName();
												indexedFileNames.put(name, resource);
											}
										//}
											return false;
		
										case IResource.FOLDER :
												if (patterns != null && Util.isExcluded(proxy.requestResource(), patterns))
													return false;
												if (hasOutputs && outputs.contains(proxy.requestFullPath())) {
													return false;
												}
										}
										return true;
								 }
								},
								IResource.NONE
							);
						} else {
							sourceFolder.accept(
								new IResourceProxyVisitor() {
									public boolean visit(IResourceProxy proxy) {
										if (isCancelled) return false;
										switch(proxy.getType()) {
											case IResource.FILE :
//												TODO: BOG Put the file name checking back
											//	if (Util.isCCFileName(proxy.getName())) {
													IResource resource = proxy.requestResource();
													IPath path = resource.getLocation();
													if (path != null && (patterns == null || !Util.isExcluded(resource, patterns))) {
														String name = new IFileDocument((IFile) resource).getName();
														indexedFileNames.put(name,
															indexedFileNames.get(name) == null || indexLastModified < path.toFile().lastModified()
																? (Object) resource
																: (Object) OK);
													}
												//}
												return false;
											case IResource.FOLDER :
												if (patterns != null && Util.isExcluded(proxy.requestResource(), patterns))
													return false;
												if (hasOutputs && outputs.contains(proxy.requestFullPath())) {
													return false;
												}
										}
										return true;
									}
								},
								IResource.NONE
							);
						}
					}
									
			Object[] names = indexedFileNames.keyTable;
			Object[] values = indexedFileNames.valueTable;
			boolean shouldSave = false;
			for (int i = 0, length = names.length; i < length; i++) {
				String name = (String) names[i];
				if (name != null) {
					if (this.isCancelled) return false;

					Object value = values[i];
					if (value != OK) {
						shouldSave = true;
					if (value == DELETED)
							this.manager.remove(name, this.dependencyTreePath);
					else{
						IScannerInfo scanInfo = null;
						IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
						if (provider != null){
							scanInfo = provider.getScannerInformation(project);
						}
						this.manager.addSource((IFile) value, this.dependencyTreePath, scanInfo);
					}
				}
			  }
			}
		} catch (/*IO*/Exception e) {
			if (DependencyManager.VERBOSE) {
				JobManager.verbose("-> failed to generate tree " + this.project + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeTree(this.dependencyTreePath);
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
	 return true;
	}

	public int hashCode() {
		  return this.project.hashCode();
	}
	
    protected Integer updatedIndexState() {
	  return DependencyManager.REBUILDING_STATE;
    }

    public String toString() {
	  return "calculating dependency tree for project " + this.project.getFullPath(); //$NON-NLS-1$
    }
}
