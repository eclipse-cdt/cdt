/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.IndexSelector;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author bgheorgh
 */
public class DependencyQueryJob implements IIndexJob {

	IProject project;
	IFile file;
	ArrayList includeFiles;
	SourceIndexer indexer;
	IndexManager indexManager;
	protected IndexSelector indexSelector;
	protected long executionTime = 0;
	
	public DependencyQueryJob(IProject project, IFile file, SourceIndexer indexer, List includeFiles) {
		this.project = project;
		this.file = file;
		this.indexer = indexer;
		this.includeFiles = (ArrayList) includeFiles;
		this.indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#belongsTo(java.lang.String)
	 */
	public boolean belongsTo(String jobFamily) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#cancel()
	 */
	public void cancel() {}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.IJob#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean execute(IProgressMonitor progressMonitor) {
		if ((project == null) ||(file == null)) return false;
		//
		if (progressMonitor != null && progressMonitor.isCanceled())
					throw new OperationCanceledException();
				boolean isComplete = COMPLETE;
				executionTime = 0;
				if (this.indexSelector == null) {
					this.indexSelector =
						new IndexSelector(SearchEngine.createWorkspaceScope(), null, false, indexManager);
				}
				IIndex[] searchIndexes = this.indexSelector.getIndexes();
				try {
					int max = searchIndexes.length;
					int min=0;
					if (progressMonitor != null) {
						progressMonitor.beginTask("", max); //$NON-NLS-1$
					}
					for (int i = 0; i < max; i++) {
						isComplete &= getFileDeps(searchIndexes[i], progressMonitor);
						if (progressMonitor != null) {
							if (progressMonitor.isCanceled()) {
								throw new OperationCanceledException();
							} else {
								progressMonitor.worked(1);
							}
						}
					}
					if (JobManager.VERBOSE) {
						JobManager.verbose("-> execution time: " + executionTime + "ms - " + this);//$NON-NLS-1$//$NON-NLS-2$
					}
					return isComplete;
				} finally {
					if (progressMonitor != null) {
						progressMonitor.done();
					}
				}
	}

	/**
	 * @param index
	 * @param progressMonitor
	 * @return
	 */
	public boolean getFileDeps(IIndex index, IProgressMonitor progressMonitor){
	
		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
	
		if (index == null)
			return COMPLETE;
		
		
		if (!(indexer instanceof SourceIndexer))
			return FAILED;
		
		
		SourceIndexer sourceIndexer = (SourceIndexer)indexer;
			
		ReadWriteMonitor monitor = sourceIndexer.getMonitorFor(index);
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read

			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					sourceIndexer.saveIndex(index);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			long start = System.currentTimeMillis();
			//
			IndexInput input = new BlocksIndexInput(index.getIndexFile());
			try {
				input.open();
				findDep(input);
			} finally {
				input.close();
			}
		
			executionTime += System.currentTimeMillis() - start;
			return COMPLETE;
		}
		catch (IOException e){
			return FAILED;
		}
		 finally {
			monitor.exitRead(); // finished reading
		}
	}
	
	/**
	 * @param input
	 * @param includeFiles
	 */
	private void findDep(IndexInput input) throws IOException {
		
		IndexedFileEntry indexedFile = input.getIndexedFile(file.getFullPath().toString());
		if (indexedFile == null) return;
		
		
		int fileNum =indexedFile.getFileID();
		IncludeEntry[] tempEntries = input.queryIncludeEntries(fileNum);
		if (tempEntries != null){
			for (int r=0; r<tempEntries.length; r++){
			   char[] tempFile = tempEntries[r].getFile();
			   StringBuffer tempString = new StringBuffer();
			   tempString.append(tempFile);
			   includeFiles.add(tempString.toString());
		   }
		}
	}

	public String toString() {
		return "searching for the dependencies of" + file.getName(); //$NON-NLS-1$
	}

	public boolean isReadyToRun() {
		if (this.indexSelector == null) { // only check once. As long as this job is used, it will keep the same index picture
			this.indexSelector = new IndexSelector(SearchEngine.createWorkspaceScope(), null, false, indexManager);
			this.indexSelector.getIndexes(); // will only cache answer if all indexes were available originally
		}
		return true;
	}

}
