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
package org.eclipse.cdt.core.browser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.browser.cache.TypeCache;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacherJob;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Manages a search cache for types in the workspace. Instead of returning objects of type <code>ICElement</code>
 * the methods of this class returns a list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache as needed.
 * If the cache becomes dirty again while the background job is running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress dialog is shown.
 * If called before the background job has finished, getTypes waits
 * for the completion of the background job.
 */
public class AllTypesCache {
	
	private static final int INITIAL_DELAY= 5000;
	private static TypeCache fgCache;
	private static TypeCacherJob fgJob;
	private static TypeCacheDeltaListener fgDeltaListener;
	
	/**
	 * Defines a simple interface in order to provide
	 * a level of abstraction between the Core and UI
	 * code.
	 */
	public static interface IWorkingCopyProvider {
		public IWorkingCopy[] getWorkingCopies();
	}
	
	/**
	 * Initializes the AllTypesCache service.
	 * 
	 * @param provider A working copy provider.
	 */
	public static void initialize(IWorkingCopyProvider provider) {
		fgCache= new TypeCache();
		fgJob= new TypeCacherJob(fgCache, provider);
		fgDeltaListener= new TypeCacheDeltaListener(fgCache, fgJob);
		
		// add delta listener
		CoreModel.getDefault().addElementChangedListener(fgDeltaListener);

		// schedule job to run after INITIAL_DELAY
		if (fgJob.getState() != Job.RUNNING) {
			fgJob.setSearchPaths(null);
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
	 * Returns the actual type cache.
	 */
	public static TypeCache getCache() {
		return fgCache;
	}
	
	/**
	 * Returns true if the type cache is up to date.
	 */
	public static boolean isCacheUpToDate() {
		return !fgCache.isDirty();
	}
	
	/**
	 * Returns all types in the given scope.
	 * @param scope The search scope
	 * @param kinds Array containing CElement types:
	 * C_NAMESPACE, C_CLASS, C_UNION, C_ENUMERATION, C_TYPEDEF
	 * @param monitor Progress monitor to display search progress
	 * @param typesFound The resulting <code>TypeInfo</code> elements are added to this collection
	 */		
	public static void getTypes(ICSearchScope scope, int[] kinds, IProgressMonitor monitor, Collection typesFound) {
		if (!isCacheUpToDate()) {
			// start job if not already running
			if (fgJob.getState() != Job.RUNNING) {
				// boost priority since action was user-initiated
				fgJob.setSearchPaths(null);
				fgJob.setPriority(Job.SHORT);
				fgJob.schedule();
			}
			
			// wait for job to finish
			try {
				fgJob.join(monitor);
				if (monitor != null)
					monitor.done();
			} catch (InterruptedException ex) {
				return;
			}
		}
		
		boolean isWorkspaceScope= scope.equals(SearchEngine.createWorkspaceScope());
		for (Iterator typesIter= fgCache.getAllTypes().iterator(); typesIter.hasNext(); ) {
			ITypeInfo info= (ITypeInfo) typesIter.next();
			if ( ArrayUtil.contains(kinds, info.getType()) &&
				(isWorkspaceScope || info.isEnclosed(scope)) ) {
				typesFound.add(info);
			}
		}
	}
	
	/**
	 * Listener for changes to CModel.
	 * @see org.eclipse.cdt.core.model.IElementChangedListener
	 * @since 3.0
	 */
	private static class TypeCacheDeltaListener implements IElementChangedListener {
		
		private TypeCache fTypeCache;
		private TypeCacherJob fTypeCacherJob;
		private Set fPaths= new HashSet(5);
		private Set fPrefixes= new HashSet(5);
		private boolean fFlushAll= false;
		
		public TypeCacheDeltaListener(TypeCache cache, TypeCacherJob job) {
			fTypeCache= cache;
			fTypeCacherJob= job;
		}
		
		/*
		 * @see IElementChangedListener#elementChanged
		 */
		public void elementChanged(ElementChangedEvent event) {
			fPaths.clear();
			fPrefixes.clear();
			fFlushAll= false;

			boolean needsFlushing= processDelta(event.getDelta());
			if (needsFlushing) {
				// cancel background job
				if (fTypeCacherJob.getState() == Job.RUNNING) {
					// wait for job to finish?
					try {
						fTypeCacherJob.cancel();
						fTypeCacherJob.join();
					} catch (InterruptedException ex) {
					}
				}
				
				if (fFlushAll) {
					// flush the entire cache
					fTypeCacherJob.setSearchPaths(null);
					fTypeCache.flushAll();
				} else {
					// flush affected files from cache
					Set searchPaths= new HashSet(10);
					getPrefixMatches(fPrefixes, searchPaths);
					searchPaths.addAll(fPaths);
					fTypeCacherJob.setSearchPaths(searchPaths);
					fTypeCache.flush(searchPaths);
				}

				// restart the background job
				fTypeCacherJob.setPriority(Job.BUILD);
				fTypeCacherJob.schedule();
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
				{
					if (isAddedOrRemoved) {
						// CModel has changed
						// flush the entire cache
						fFlushAll= true;
						return true;
					}
					return processDeltaChildren(delta);
				}
				
				case ICElement.C_PROJECT:
				case ICElement.C_CCONTAINER:
				{
					if (isAddedOrRemoved) {
						// project or folder has changed
						// flush all files with matching prefix
						IPath path= elem.getPath();
						if (path != null)
							fPrefixes.add(path);
						return true;
					}
					return processDeltaChildren(delta);
				}
				
				case ICElement.C_NAMESPACE:
				case ICElement.C_TEMPLATE_CLASS:
				case ICElement.C_CLASS:
				case ICElement.C_STRUCT:
				case ICElement.C_UNION:
				case ICElement.C_ENUMERATION:
				case ICElement.C_TYPEDEF:
				case ICElement.C_INCLUDE:
				case ICElement.C_UNIT:
				{
					if (isAddedOrRemoved) {
						// CElement has changed
						// flush file from cache
						IPath path= elem.getPath();
						if (path != null)
							fPaths.add(path);
						return true;
					}
					return processDeltaChildren(delta);
				}
					
				default:
					// fields, methods, imports ect
					return false;
			}	
		}

		private boolean processDeltaChildren(ICElementDelta delta) {
			ICElementDelta[] children= delta.getAffectedChildren();
			for (int i= 0; i < children.length; i++) {
				if (processDelta(children[i])) {
					return true;
				}
			}
			return false;
		}
		
		private boolean getPrefixMatches(Set prefixes, Set results) {
			Set pathSet= fTypeCache.getAllFiles();
			if (pathSet.isEmpty() || prefixes == null || prefixes.isEmpty())
				return false;

			for (Iterator pathIter= pathSet.iterator(); pathIter.hasNext(); ) {
				IPath path= (IPath) pathIter.next();

				// find paths which match prefix
				for (Iterator prefixIter= prefixes.iterator(); prefixIter.hasNext(); ) {
					IPath prefix= (IPath) prefixIter.next();
					if (prefix.isPrefixOf(path)) {
						results.add(path);
						break;
					}
				}
			}

			return !results.isEmpty();
		}
	}	
}
