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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

public class TypeCacheManager {
	
	private static final TypeCacheManager fgInstance = new TypeCacheManager();
	private Map fCacheMap;
	private IWorkingCopyProvider fWorkingCopyProvider;

	private TypeCacheManager() {
		fCacheMap = new HashMap();
	}
	
	public static TypeCacheManager getInstance() {
		return fgInstance;
	}
	
	public void setWorkingCopyProvider(IWorkingCopyProvider workingCopyProvider) {
		fWorkingCopyProvider = workingCopyProvider;
	}

	public synchronized void updateProject(IProject project) {
		TypeCacheDelta cacheDelta = new TypeCacheDelta(project);
		getCache(project).addDelta(cacheDelta);
	}

	private static final int PATH_ENTRY_FLAGS = ICElementDelta.F_ADDED_PATHENTRY_SOURCE
		| ICElementDelta.F_REMOVED_PATHENTRY_SOURCE
		| ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE
		| ICElementDelta.F_CHANGED_PATHENTRY_MACRO
		| ICElementDelta.F_PATHENTRY_REORDER;

	public synchronized void processDelta(ICElementDelta delta) {
		ICElement elem = delta.getElement();
		boolean added = (delta.getKind() == ICElementDelta.ADDED);
		boolean removed = (delta.getKind() == ICElementDelta.REMOVED);
		boolean contentChanged = ((delta.getFlags() & ICElementDelta.F_CONTENT) != 0);
		boolean pathEntryChanged = ((delta.getFlags() & PATH_ENTRY_FLAGS) != 0);
		boolean hasChildren = ((delta.getFlags() & ICElementDelta.F_CHILDREN) != 0);

		switch (elem.getElementType()) {
			case ICElement.C_PROJECT:
			case ICElement.C_CCONTAINER: {
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				if (added || removed || pathEntryChanged) {
					TypeCacheDelta cacheDelta = new TypeCacheDelta(project, delta);
					getCache(project).addDelta(cacheDelta);
				}
			}
			break;
			
			case ICElement.C_UNIT: {
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				ITranslationUnit unit = (ITranslationUnit) elem;
				if (unit.isWorkingCopy()) {
					processWorkingCopyDelta(delta);
					return;
				} else {
					if (added || removed || pathEntryChanged || contentChanged) {
						TypeCacheDelta cacheDelta = new TypeCacheDelta(project, delta);
						getCache(project).addDelta(cacheDelta);
					}
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
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				if (added || removed) {
					TypeCacheDelta cacheDelta = new TypeCacheDelta(project, delta);
					getCache(project).addDelta(cacheDelta);
				}
			}
			break;
		}

		if (hasChildren) {
			ICElementDelta[] children = delta.getAffectedChildren();
			if (children != null) {
				for (int i = 0; i < children.length; ++i) {
					processDelta(children[i]);
				}
			}
		}
	}
	
	public synchronized void processWorkingCopyDelta(ICElementDelta delta) {
		// ignore workies copies for now
		return;
/*		ICElement elem = delta.getElement();
		boolean added = (delta.getKind() == ICElementDelta.ADDED);
		boolean removed = (delta.getKind() == ICElementDelta.REMOVED);
		boolean contentChanged = ((delta.getFlags() & ICElementDelta.F_CONTENT) != 0);
		boolean pathEntryChanged = ((delta.getFlags() & PATH_ENTRY_FLAGS) != 0);
		boolean hasChildren = ((delta.getFlags() & ICElementDelta.F_CHILDREN) != 0);

		switch (elem.getElementType()) {
			case ICElement.C_UNIT: {
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				if (added || removed || pathEntryChanged || contentChanged) {
					TypeCacheDelta cacheDelta = new TypeCacheDelta(project, delta);
					getCache(project).addDelta(cacheDelta);
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
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				if (added || removed) {
					TypeCacheDelta cacheDelta = new TypeCacheDelta(project, delta);
					getCache(project).addDelta(cacheDelta);
				}
			}
			break;
		}

		if (hasChildren) {
			ICElementDelta[] children = delta.getAffectedChildren();
			if (children != null) {
				for (int i = 0; i < children.length; ++i) {
					processWorkingCopyDelta(children[i]);
				}
			}
		}
*/
	}
	
	public synchronized ITypeCache getCache(IProject project) {
		synchronized(fCacheMap) {
			ITypeCache cache = (ITypeCache) fCacheMap.get(project);
			if (cache == null) {
				cache = new TypeCache(project, fWorkingCopyProvider);
				fCacheMap.put(project, cache);
			}
			return cache;
		}
	}
	
	public synchronized void reconcile(boolean enableIndexing, int priority, int delay) {
		TypeSearchScope workspaceScope = new TypeSearchScope(true);
		IProject[] projects = workspaceScope.getEnclosingProjects();
		for (int i = 0; i < projects.length; ++i) {
			ITypeCache cache = getCache(projects[i]);
			cache.reconcile(enableIndexing, priority, delay);
		}
	}

	public synchronized void reconcileAndWait(boolean enableIndexing, int priority, IProgressMonitor monitor) {
		TypeSearchScope workspaceScope = new TypeSearchScope(true);
		IProject[] projects = workspaceScope.getEnclosingProjects();
		for (int i = 0; i < projects.length; ++i) {
			ITypeCache cache = getCache(projects[i]);
			cache.reconcileAndWait(enableIndexing, priority, monitor);
		}
	}

	public void cancelJobs() {
		IJobManager jobManager = Platform.getJobManager();
		jobManager.cancel(TypeCacherJob.FAMILY);
		jobManager.cancel(TypeLocatorJob.FAMILY);
	}
	
	public ITypeInfo[] locateSuperTypesAndWait(ITypeInfo info, boolean enableIndexing, int priority, IProgressMonitor monitor) {
		ITypeInfo[] superTypes = info.getSuperTypes();
		if (superTypes == null) {
			// cancel background jobs
			IProject project = info.getEnclosingProject();
			getCache(project).cancelJobs();

			// start the search job
			getCache(project).locateSupertypesAndWait(info, priority, monitor);
			
			superTypes = info.getSuperTypes();

			// resume background jobs
			reconcile(enableIndexing, Job.BUILD, 0);
		}
		return superTypes;
	}

	public ITypeInfo[] locateSubTypesAndWait(ITypeInfo info, boolean enableIndexing, int priority, IProgressMonitor monitor) {
		ITypeInfo[] subTypes = info.getSubTypes();
		if (subTypes == null) {
			// cancel background jobs
			IProject project = info.getEnclosingProject();
			getCache(project).cancelJobs();

			// start the search job
			getCache(project).locateSubtypesAndWait(info, priority, monitor);
			
			subTypes = info.getSubTypes();

			// resume background jobs
			reconcile(enableIndexing, Job.BUILD, 0);
		}
		return subTypes;
	}
}
