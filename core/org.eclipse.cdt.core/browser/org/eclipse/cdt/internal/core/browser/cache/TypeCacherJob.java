/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;


/**
 * Background job for filling the type cache.
 * @see org.eclipse.core.runtime.jobs.Job
 * @since 3.0
 */
public class TypeCacherJob extends BasicJob {
	
	public static final Object FAMILY = new Object();
	private IndexManager fIndexManager;
	private ITypeCache fTypeCache;
	private TypeCacheDelta[] fDeltas;
	private boolean fEnableIndexing;
	private boolean fIndexerIsBusy;

	public TypeCacherJob(ITypeCache typeCache, TypeCacheDelta[] deltas, boolean enableIndexing) {
		super(TypeCacheMessages.getString("TypeCacherJob.defaultJobName"), FAMILY); //$NON-NLS-1$
		fTypeCache = typeCache;
		fDeltas = deltas;
		fEnableIndexing = enableIndexing;
		fIndexerIsBusy = false;
		fIndexManager = CModelManager.getDefault().getIndexManager();
		setPriority(BUILD);
		setSystem(true);
		setRule(typeCache);
		setName(TypeCacheMessages.getFormattedString("TypeCacherJob.jobName", fTypeCache.getProject().getName())); //$NON-NLS-1$
	}
	
	public ITypeCache getCache() {
		return fTypeCache;
	}

	public TypeCacheDelta[] getDeltas() {
		return fDeltas;
	}

	public boolean isIndexerBusy() {
	    return fIndexerIsBusy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(IProgressMonitor)
	 */
	protected IStatus runWithDelegatedProgress(IProgressMonitor monitor) throws InterruptedException {
		boolean success = false;
		long startTime = System.currentTimeMillis();
		trace("TypeCacherJob: started"); //$NON-NLS-1$
		
		try {
			int totalWork = 100;
			monitor.beginTask(TypeCacheMessages.getString("TypeCacherJob.taskName"), totalWork); //$NON-NLS-1$
			
			// figure out what needs to be flushed
			TypeSearchScope flushScope = new TypeSearchScope();
			if (fDeltas != null) {
				for (int i = 0; i < fDeltas.length; ++i) {
					TypeCacheDelta delta = fDeltas[i];
					prepareToFlush(delta, flushScope);
				}
			}

			if (monitor.isCanceled())
				throw new InterruptedException();

			// flush the cache
			int flushWork = 0;
			if (!flushScope.isEmpty()) {
				flushWork = totalWork / 4;
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, flushWork);
				flush(flushScope, subMonitor);
			}
			
			// update the cache
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, totalWork - flushWork);
			update(flushScope, subMonitor);
			
			if (monitor.isCanceled())
				throw new InterruptedException();
		} finally {
			long executionTime = System.currentTimeMillis() - startTime;
			if (success)
				trace("TypeCacherJob: completed ("+ executionTime + " ms)"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				trace("TypeCacherJob: aborted ("+ executionTime + " ms)"); //$NON-NLS-1$ //$NON-NLS-2$

			monitor.done();
		}
		
		return Status.OK_STATUS;
	}
	
	private void flush(ITypeSearchScope scope, IProgressMonitor monitor) throws InterruptedException {
		// flush the cache
		boolean success = true;
		IProject project = fTypeCache.getProject();

		monitor.beginTask("", 100); //$NON-NLS-1$
		
		fTypeCache.flush(scope);
		if (!scope.encloses(project)) {
			if (project.exists() && project.isOpen()) {
			    success = doIndexerJob(new IndexerDependenciesJob(fIndexManager, fTypeCache, scope), monitor);
			}
		}

		if (!success || monitor.isCanceled()) {
			throw new InterruptedException();
		}
		
		monitor.done();
	}
	
	private void update(ITypeSearchScope scope, IProgressMonitor monitor) throws InterruptedException {
		boolean success = true;
		IProject project = fTypeCache.getProject();
	
		monitor.beginTask("", 100); //$NON-NLS-1$
		if (project.exists() && project.isOpen()) {
		    success = doIndexerJob(new IndexerTypesJob2(fIndexManager, fTypeCache, scope), monitor);
		}
		
		if (!success || monitor.isCanceled()) {
			throw new InterruptedException();
		}
			
		monitor.done();
	}
	
	private boolean doIndexerJob(IndexerJob job, IProgressMonitor monitor) {
		if (!fEnableIndexing) {
		    return false;
		}
		
		// check if indexer is busy
		fIndexerIsBusy = false;
		try {
			fIndexManager.performConcurrentJob(new DummyIndexerJob(fIndexManager, fTypeCache.getProject()),
				ICSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH, new NullProgressMonitor(), null);
		} catch (OperationCanceledException e) {
	        fIndexerIsBusy = true;
		}
		
		// do an immediate (but possibly incomplete) search
	    // if fIndexerIsBusy the cache will stay dirty and we'll hit the indexer again next time
		return fIndexManager.performConcurrentJob(job,
			ICSearchConstants.FORCE_IMMEDIATE_SEARCH, monitor, null);
	}

    private boolean doIndexerJob(IndexerJob2 job, IProgressMonitor monitor) {
        if (!fEnableIndexing) {
            return false;
        }
        
        // check if indexer is busy
        fIndexerIsBusy = false;
        try {
            fIndexManager.performConcurrentJob(new DummyIndexerJob(fIndexManager, fTypeCache.getProject()),
                ICSearchConstants.CANCEL_IF_NOT_READY_TO_SEARCH, new NullProgressMonitor(), null);
        } catch (OperationCanceledException e) {
            fIndexerIsBusy = true;
        }
        
        // do an immediate (but possibly incomplete) search
        // if fIndexerIsBusy the cache will stay dirty and we'll hit the indexer again next time
        return fIndexManager.performConcurrentJob(job,
            ICSearchConstants.FORCE_IMMEDIATE_SEARCH, monitor, null);
    }

    
	private static final int PATH_ENTRY_FLAGS = ICElementDelta.F_ADDED_PATHENTRY_SOURCE
		| ICElementDelta.F_REMOVED_PATHENTRY_SOURCE
		| ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE
		| ICElementDelta.F_CHANGED_PATHENTRY_MACRO
		| ICElementDelta.F_PATHENTRY_REORDER;
	
	private void prepareToFlush(TypeCacheDelta cacheDelta, ITypeSearchScope scope) {
		ITypeSearchScope deltaScope = cacheDelta.getScope();
		if (deltaScope != null)
			scope.add(deltaScope);

		ICElementDelta delta = cacheDelta.getCElementDelta();
		if (delta != null) {
			ICElement elem = delta.getElement();
			boolean added = (delta.getKind() == ICElementDelta.ADDED);
			boolean removed = (delta.getKind() == ICElementDelta.REMOVED);
			boolean contentChanged = ((delta.getFlags() & ICElementDelta.F_CONTENT) != 0);
			boolean pathEntryChanged = ((delta.getFlags() & PATH_ENTRY_FLAGS) != 0);
			
			switch (elem.getElementType()) {
				case ICElement.C_MODEL:	{
					if (added || removed) {
						scope.add(elem);
					}
				}
				break;
				
				case ICElement.C_PROJECT: {
					if (added || removed || pathEntryChanged) {
						scope.add(elem);
					}
				}
				break;
				
				case ICElement.C_CCONTAINER: {
					if (added || removed || pathEntryChanged) {
						scope.add(elem);
					}
				}
				break;
				
				case ICElement.C_UNIT: {
					if (added || removed || pathEntryChanged || contentChanged) {
						scope.add(elem);
					}
				}
				break;
				
				case ICElement.C_INCLUDE:
				case ICElement.C_NAMESPACE:
				case ICElement.C_TEMPLATE_CLASS:
				case ICElement.C_CLASS:
				case ICElement.C_STRUCT:
				case ICElement.C_UNION:
				case ICElement.C_ENUMERATION:
				case ICElement.C_TYPEDEF:
				{
					//TODO handle working copies
					if (added || removed) {
						scope.add(elem);
					}
				}
				break;
			}
		}
	}
	
	private static final class DummyIndexerJob extends IndexerJob {
	    public DummyIndexerJob(IndexManager indexManager, IProject project) {
	        super(indexManager, project);
	    }
		protected boolean processIndex(IIndex index, IProject project, IProgressMonitor progressMonitor) throws InterruptedException {
		    return false;
		}
	}
	
}

