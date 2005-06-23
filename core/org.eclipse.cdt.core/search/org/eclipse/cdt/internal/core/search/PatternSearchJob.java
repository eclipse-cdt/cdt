/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.cdt.internal.core.search;

import java.io.IOException;

import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;


public class PatternSearchJob implements IIndexJob {


	protected CSearchPattern pattern;
	protected ICSearchScope scope;
	protected ICElement focus;
	protected IIndexSearchRequestor requestor;
	protected IndexManager indexManager;
	protected int detailLevel;
	protected IndexSelector indexSelector;
	protected boolean isPolymorphicSearch;
	protected long executionTime = 0;
	
	public PatternSearchJob(
		CSearchPattern pattern,
		ICSearchScope scope,
		IIndexSearchRequestor requestor,
		IndexManager indexManager) {

		this(
			pattern,
			scope,
			null,
			false,
			requestor,
			indexManager);
	}
	public PatternSearchJob(
		CSearchPattern pattern,
		ICSearchScope scope,
		ICElement focus,
		boolean isPolymorphicSearch,
		IIndexSearchRequestor requestor,
		IndexManager indexManager) {

		this.pattern = pattern;
		this.scope = scope;
		this.focus = focus;
		this.isPolymorphicSearch = isPolymorphicSearch;
		this.requestor = requestor;
		this.indexManager = indexManager;
	}
	public boolean belongsTo(String jobFamily) {
		return true;
	}
	public void cancel() {
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
		boolean isComplete = COMPLETE;
		executionTime = 0;
		if (this.indexSelector == null) {
			this.indexSelector =
				new IndexSelector(this.scope, this.focus, this.isPolymorphicSearch, this.indexManager);
		}
		IIndex[] searchIndexes = this.indexSelector.getIndexes();
		try {
			int max = searchIndexes.length;
			if (progressMonitor != null) {
				progressMonitor.beginTask("", max); //$NON-NLS-1$
			}
			for (int i = 0; i < max; i++) {
				isComplete &= search(searchIndexes[i], progressMonitor);
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
	public boolean isReadyToRun() {
		if (this.indexSelector == null) { // only check once. As long as this job is used, it will keep the same index picture
			this.indexSelector = new IndexSelector(this.scope, this.focus, this.isPolymorphicSearch, this.indexManager);
			this.indexSelector.getIndexes(); // will only cache answer if all indexes were available originally
		}
		return true;
	}
	public boolean search(IIndex index, IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

//		IIndex inMemIndex = indexManager.peekAtIndex(new Path(((Index)index).toString.substring("Index for ".length()).replace('\\','/')));
//		if (inMemIndex != index) {
//			System.out.println("SANITY CHECK: search job using obsolete index: ["+index+ "] instead of: ["+inMemIndex+"]");
//		}
		
		if (index == null)
			return COMPLETE;
		
		if (!(index instanceof Index))
			return FAILED;
			
		ICDTIndexer indexer =((Index) index).getIndexer();
		
		IIndexStorage storage = indexer.getIndexStorage();
		if (!(storage instanceof CIndexStorage))
			return FAILED;
		
		CIndexStorage cStorage = (CIndexStorage) storage;
		ReadWriteMonitor monitor = cStorage.getMonitorForIndex();
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read

			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					cStorage.saveIndex(index);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			long start = System.currentTimeMillis();
			pattern.findIndexMatches(
				index,
				requestor,
				detailLevel,
				progressMonitor,
				this.scope);
			executionTime += System.currentTimeMillis() - start;
			return COMPLETE;
		} catch (IOException e) {
			return FAILED;
		} finally {
			monitor.exitRead(); // finished reading
		}
	}
	public String toString() {
		return "searching " + pattern.toString(); //$NON-NLS-1$
	}
}
