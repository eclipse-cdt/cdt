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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.AllTypesCache.IWorkingCopyProvider;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.browser.util.DelegatedProgressMonitor;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.CWorkspaceScope;
import org.eclipse.cdt.internal.core.search.PatternSearchJob;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;


/**
 * Background job for filling the type cache.
 * @see org.eclipse.core.runtime.jobs.Job
 * @since 3.0
 */
public class TypeCacherJob extends Job {

	/**
	 * An "identity rule" that forces jobs to be queued.
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule
	 * @since 3.0
	 */
	final static ISchedulingRule MUTEX_RULE= new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}			
	};
	
	/**
	 * Constant identifying the job family identifier for the background job.
	 * @see IJobManager#join(Object, IProgressMonitor)
	 * @since 3.0
	 */
	private static final Object FAMILY= new Object();

	private DelegatedProgressMonitor fProgressMonitor;
	private Set fSearchPaths= new HashSet(50);
	private TypeCache fTypeCache;
	private IWorkingCopyProvider fWorkingCopyProvider;

	public TypeCacherJob(TypeCache cache, IWorkingCopyProvider provider) {
		super(TypeCacheMessages.getString("TypeCacherJob.jobName")); //$NON-NLS-1$
		setPriority(BUILD);
		setSystem(true);
		//setRule(MUTEX_RULE);
		fTypeCache= cache;
		fWorkingCopyProvider= provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		return family == FAMILY;
	}
	
	public void setSearchPaths(Set paths) {
		fSearchPaths.clear();
		if (paths != null)
			fSearchPaths.addAll(paths);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		fProgressMonitor= new DelegatedProgressMonitor(monitor);
		
		try {
			search(fProgressMonitor);
			fProgressMonitor.done();
		} catch(InterruptedException ex) {
			return Status.CANCEL_STATUS;
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} finally {
			fProgressMonitor= null;
		}

		return Status.OK_STATUS;
	}
	
	/**
	 * Forwards progress info to the progress monitor and
	 * blocks until the job is finished.
	 * 
	 * @param monitor Optional progress monitor.
	 * @throws InterruptedException
	 * 
	 * @see Job#join
	 */
	public void join(IProgressMonitor monitor) throws InterruptedException {
		if (fProgressMonitor != null)
			fProgressMonitor.addDelegate(monitor);
		super.join();
	}
	
	private void search(IProgressMonitor monitor) throws InterruptedException {

		monitor.beginTask(TypeCacheMessages.getString("TypeCacherJob.taskName"), 100); //$NON-NLS-1$

		IWorkspace workspace= CCorePlugin.getWorkspace();
		if (workspace == null)
			throw new InterruptedException();

		ICSearchScope scope= new CWorkspaceScope();
		
		// search for types and #include references
		TypeSearchPattern pattern= new TypeSearchPattern();
		for (Iterator pathIter= fSearchPaths.iterator(); pathIter.hasNext(); ) {
			IPath path= (IPath) pathIter.next();
			pattern.addDependencySearch(path);
		}
		TypeSearchPathCollector pathCollector= new TypeSearchPathCollector();
		
		CModelManager modelManager= CModelManager.getDefault();
		IndexManager indexManager= modelManager.getIndexManager();
	
		if (monitor.isCanceled())
			throw new InterruptedException();

		SubProgressMonitor subMonitor= new SubProgressMonitor(monitor, 5);
		
		/* index search */
		indexManager.performConcurrentJob( 
			new PatternSearchJob(
				pattern,
				scope,
				pathCollector,
				indexManager
			),
		    ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			subMonitor,
			null );
		
		if (monitor.isCanceled())
			throw new InterruptedException();
		
		if (!fSearchPaths.isEmpty()) {
			// flush all affected files from cache
			fTypeCache.flush(fSearchPaths);
			Set dependencyPaths= pathCollector.getDependencyPaths();
			if (dependencyPaths != null && !dependencyPaths.isEmpty()) {
				fTypeCache.flush(dependencyPaths);
			}
		}

		Set allSearchPaths= pathCollector.getPaths();
		if (allSearchPaths == null)
			allSearchPaths= new HashSet();
		
		// remove cached files
		allSearchPaths.removeAll(fTypeCache.getAllFiles());
		
		if (monitor.isCanceled())
			throw new InterruptedException();

		subMonitor= new SubProgressMonitor(monitor, 95);
		
		IWorkingCopy[] workingCopies= null;
		if (fWorkingCopyProvider != null)
			workingCopies= fWorkingCopyProvider.getWorkingCopies();

		TypeMatchCollector collector= new TypeMatchCollector(fTypeCache, subMonitor);
		TypeMatchLocator matchLocator= new TypeMatchLocator(collector);
		matchLocator.locateMatches(allSearchPaths, workspace, workingCopies);
		
		if (monitor.isCanceled())
			throw new InterruptedException();

		fTypeCache.markAsDirty(false);
		monitor.done();
	}
}
