/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - adapted for use in CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.browser.typeinfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.browser.util.ArrayUtil;
import org.eclipse.cdt.internal.ui.browser.util.ProgressMonitorMultiWrapper;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Manages a search cache for types in the workspace. Instead of returning objects of type <code>ICElement</code>
 * the methods of this class returns a list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache as needed.
 * If the cache becomes dirty again while the background job is running, the job is restarted.
 * <P>
 * If <code>getAllTypes</code> is called in response to a user action, a progress dialog is shown.
 * If called before the background job has finished, getAllTypes waits
 * for the completion of the background job.
 */
public class AllTypesCache {
	
	/**
	 * Background job for filling the type cache.
	 * @see org.eclipse.core.runtime.jobs.Job
	 * @since 3.0
	 */
	private static class TypeCacherJob extends Job {

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
		 * A comparator for simple type names
		 */
		private static class TypeNameComparator implements Comparator {
			public int compare(Object o1, Object o2) {
				return ((TypeInfo)o1).getName().compareTo(((TypeInfo)o2).getName());
			}
		}
		
		/**
		 * A search result collector for type info.
		 * @see org.eclipse.cdt.core.search.ICSearchResultCollector
		 */
		private static class TypeSearchResultCollector extends BasicSearchResultCollector {

			public TypeSearchResultCollector() {
				super();
			}
			
			public TypeSearchResultCollector(IProgressMonitor monitor) {
				super(monitor);
			}
			
			public IMatch createMatch(Object fileResource, int start, int end, ISourceElementCallbackDelegate node, IPath realPath )
			{
				TypeInfo result= new TypeInfo();
				return super.createMatch( result, fileResource, start, end, node, realPath);
			}

			public boolean acceptMatch(IMatch match) throws CoreException {
				// filter out unnamed structs
				TypeInfo result= (TypeInfo) match;
				String name= result.getName();
				if (name == null || name.length() == 0)
					return false;

				// make sure we've got a valid type
				if (!TypeInfo.isValidCElementType(result.getElementType()))
					return false;

				return super.acceptMatch(match);
			}
		}		

		/**
		 * Constant identifying the job family identifier for the background job.
		 * @see IJobManager#join(Object, IProgressMonitor)
		 * @since 3.0
		 */
		public static final Object FAMILY= new Object();

		final static Comparator TYPE_COMPARATOR= new TypeNameComparator();

		private ProgressMonitorMultiWrapper progressMonitor;
		
		public TypeCacherJob() {
			super(TypeInfoMessages.getString("TypeCacherJob.jobName")); //$NON-NLS-1$
			setPriority(BUILD);
			setSystem(true);
			//setRule(MUTEX_RULE);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
		 */
		public boolean belongsTo(Object family) {
			return family == FAMILY;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		public boolean shouldRun() {
			return isCacheDirty();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			progressMonitor= new ProgressMonitorMultiWrapper(monitor);
			progressMonitor.beginTask(TypeInfoMessages.getString("TypeCacherJob.taskName"), 100); //$NON-NLS-1$

			SubProgressMonitor subMonitor= new SubProgressMonitor(progressMonitor, 100);
			TypeSearchResultCollector collector= new TypeSearchResultCollector(subMonitor);

			IWorkspace workspace= CCorePlugin.getWorkspace();
			ICSearchScope scope= SearchEngine.createWorkspaceScope();
			SearchEngine engine= new SearchEngine();
			
			ICSearchPattern pattern= createSearchPattern();
			try {
				flushCache();
				// start the search engine
				engine.search(workspace, pattern, scope, collector, true);
				if (progressMonitor.isCanceled())
					throw new InterruptedException();
				progressMonitor.done();
			} catch(InterruptedException ex) {
				return Status.CANCEL_STATUS;
			} finally {
				progressMonitor= null;
			}

			Set searchResults= collector.getSearchResults();

			if (searchResults != null) {
				TypeInfo[] result= (TypeInfo[]) searchResults.toArray(new TypeInfo[searchResults.size()]);
				Arrays.sort(result, TYPE_COMPARATOR);
				setCache(result);
			}
			else {
				TypeInfo[] result= new TypeInfo[0];
				setCache(result);
			}
			return Status.OK_STATUS;
		}

		/*
		 * creates a search pattern based on the cache types
		 */
		private ICSearchPattern createSearchPattern() {
			OrPattern pattern= new OrPattern();
			int[] types= getCacheTypes();
			for (int i= 0; i < types.length; ++i) {
				switch (types[i]) {
					case ICElement.C_NAMESPACE:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.NAMESPACE, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;

					case ICElement.C_CLASS: // fall through
					case ICElement.C_TEMPLATE_CLASS:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.CLASS, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;
					
					case ICElement.C_STRUCT: // fall through
					case ICElement.C_TEMPLATE_STRUCT:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.STRUCT, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;

					case ICElement.C_UNION: // fall through
					case ICElement.C_TEMPLATE_UNION:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.UNION, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;

					case ICElement.C_ENUMERATION:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;

					case ICElement.C_TYPEDEF:
						pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.TYPEDEF, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
					break;
					
					default:
					break;
				}
			}
			return pattern;
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
			if (progressMonitor != null)
				progressMonitor.addProgressMonitor(monitor);
			super.join();
		}
	}

	/**
	 * Listener for changes to CModel.
	 * @see org.eclipse.cdt.core.model.IElementChangedListener
	 * @since 3.0
	 */
	private static class TypeCacheDeltaListener implements IElementChangedListener {
		/*
		 * @see IElementChangedListener#elementChanged
		 */
		public void elementChanged(ElementChangedEvent event) {
			//TODO optimization: calculate deltas per file and
			// update the cache selectively
			boolean needsFlushing= processDelta(event.getDelta());
			if (needsFlushing) {
				// mark cache as dirty and reschedule the
				// background job
				setCacheDirty();
				if (fgJob.getState() == Job.RUNNING)
					fgJob.cancel();
				fgJob.setPriority(Job.BUILD);
				fgJob.schedule();
			}
		}
		
		/*
		 * returns true if the cache needs to be flushed
		 */
		private boolean processDelta(ICElementDelta delta) {
			ICElement elem= delta.getElement();
			int pathEntryChanged= ICElementDelta.F_ADDED_PATHENTRY_SOURCE | ICElementDelta.F_REMOVED_PATHENTRY_SOURCE |
									ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE | ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
			boolean isAddedOrRemoved= (delta.getKind() != ICElementDelta.CHANGED)
			 || ((delta.getFlags() & pathEntryChanged) != 0);
			
			switch (elem.getElementType()) {
				case ICElement.C_MODEL:
				case ICElement.C_PROJECT:
				case ICElement.C_CCONTAINER:
				case ICElement.C_NAMESPACE:
				case ICElement.C_TEMPLATE_CLASS:
				case ICElement.C_CLASS:
				case ICElement.C_STRUCT:
				case ICElement.C_UNION:
				case ICElement.C_ENUMERATION:
				case ICElement.C_TYPEDEF:
				case ICElement.C_INCLUDE:
				case ICElement.C_UNIT:
					if (isAddedOrRemoved) {
						return true;
					}				
					return processChildrenDelta(delta);
				default:
					// fields, methods, imports ect
					return false;
			}	
		}
		
		private boolean isPossibleStructuralChange(int flags) {
			return (flags & (ICElementDelta.F_CONTENT | ICElementDelta.F_FINE_GRAINED)) == ICElementDelta.F_CONTENT;
		}		
		
		private boolean processChildrenDelta(ICElementDelta delta) {
			ICElementDelta[] children= delta.getAffectedChildren();
			for (int i= 0; i < children.length; i++) {
				if (processDelta(children[i])) {
					return true;
				}
			}
			return false;
		}
	}
	
	private static final int INITIAL_DELAY= 5000;
	private static final TypeCacherJob fgJob= new TypeCacherJob();
	private static final TypeCacheDeltaListener fgDeltaListener= new TypeCacheDeltaListener();
	private static int[] fgCacheTypes= TypeInfo.getAllCElementTypes();
	private static TypeInfo[] fgCacheData;
	private static int fgNumberOfCacheFlushes;
	private static boolean cacheIsDirty= true;
	
	/**
	 * Initializes the AllTypesCache service.
	 */
	public static void initialize() {
		// add delta listener
		CoreModel.getDefault().addElementChangedListener(fgDeltaListener);

		// schedule job to run after INITIAL_DELAY
		if (fgJob.getState() != Job.RUNNING) {
			fgJob.setPriority(Job.BUILD);
			fgJob.schedule(INITIAL_DELAY);
		}
	}
	
	/**
	 * Terminates the service provided by AllTypesCache.
	 */
	public static void terminate() {
		// remove delta listener
		CoreModel.getDefault().removeElementChangedListener(fgDeltaListener);
		
		// terminate background job
		fgJob.cancel();
	}

	/*
	 * Sets the cache contents.
	 */
	private static synchronized void setCache(TypeInfo[] cache) {
		fgCacheData= cache;
		cacheIsDirty= false;
	}
	
	/*
	 * Gets the cache contents.
	 */
	private static synchronized TypeInfo[] getCache() {
		return fgCacheData;
	}
	
	/*
	 * Clears the cache.
	 */
	private static synchronized void flushCache() {
		fgCacheData= null;
		++fgNumberOfCacheFlushes;
		cacheIsDirty= true;
	}
	
	/*
	 * Marks cache as dirty.
	 */
	private static synchronized void setCacheDirty() {
		cacheIsDirty= true;
	}

	/*
	 * Tests if cache is dirty.
	 */
	private static synchronized boolean isCacheDirty() {
		return cacheIsDirty;
	}

	/*
	 * Sets which types are stored in the cache.
	 */
	private static synchronized void setCacheTypes(int[] cElementTypes) {
		fgCacheTypes= ArrayUtil.clone(cElementTypes);
	}
	
	/*
	 * Gets types stored in the cache.
	 */
	private static synchronized int[] getCacheTypes() {
		return fgCacheTypes;
	}

	/**
	 * Returns all types in the given scope, matching the given filter.
	 * @param filter Filter for the type info.
	 * @param monitor Progress monitor.
	 * @param typesFound The resulting <code>TypeInfo</code> elements are added to this collection
	 */		
	public static void getTypes(ICSearchScope scope, ITypeInfoFilter filter, IProgressMonitor monitor, Collection typesFound) {
		TypeInfo[] allTypes= getAllTypes(filter, monitor);
		if (allTypes != null) {
			boolean isWorkspaceScope= scope.equals(SearchEngine.createWorkspaceScope());
			for (int i= 0; i < allTypes.length; i++) {
				TypeInfo info= allTypes[i];
				if (isWorkspaceScope || info.isEnclosed(scope)) {
					if (filter.match(info))
						typesFound.add(info);
				}
			}
		}
	}

	/**
	 * Returns all types in the workspace. The returned array must not be
	 * modified. The elements in the array are sorted by simple type name.
	 */
	public static TypeInfo[] getAllTypes(ITypeInfoFilter filter, IProgressMonitor monitor) {

		// check if requested types are in cache
		if (!ArrayUtil.containsAll(getCacheTypes(), filter.getCElementTypes()))
		{
			// mark cache dirty and cancel the running job
			setCacheDirty();
			if (fgJob.getState() == Job.RUNNING)
				fgJob.cancel();
			setCacheTypes(filter.getCElementTypes());
		}
		
		if (isCacheDirty()) {
			// start job if not already running
			if (fgJob.getState() != Job.RUNNING) {
				// boost priority since action was user-initiated
				fgJob.setPriority(Job.SHORT);
				fgJob.schedule();
			}

			// wait for job to finish
			try {
				fgJob.join(monitor);
				if (monitor != null)
					monitor.done();
			} catch (InterruptedException ex) {
				return null;
			}
		}
		return getCache();
	}
		
	/**
	 * Returns true if the type cache is up to date.
	 */
	public static boolean isCacheUpToDate(ITypeInfoFilter filter) {
		// check if requested types are in cache
		if (!ArrayUtil.containsAll(getCacheTypes(), filter.getCElementTypes()))
			return false;
		return !isCacheDirty();
	}
}
