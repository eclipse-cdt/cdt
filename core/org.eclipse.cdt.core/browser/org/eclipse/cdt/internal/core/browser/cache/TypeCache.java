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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeInfoVisitor;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class TypeCache implements ITypeCache {

	private static final int INITIAL_TYPE_COUNT = 100;
	private final Map fTypeNameMap = new HashMap(INITIAL_TYPE_COUNT);
	private final IProject fProject;
	private final IWorkingCopyProvider fWorkingCopyProvider;
	private final Collection fDeltas = new ArrayList();

	private IJobChangeListener fJobChangeListener = new IJobChangeListener() {
		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void done(IJobChangeEvent event) {
			Job job = event.getJob();
			if (job instanceof TypeCacherJob) {
				TypeCacherJob deltaJob = (TypeCacherJob)job;
				IStatus status = event.getResult();
				if (status != null) {
					boolean jobFinished = status.equals(Status.OK_STATUS);
					// remove the completed deltas
					synchronized(fDeltas) {
						for (Iterator i = fDeltas.iterator(); i.hasNext(); ) {
							TypeCacheDelta delta = (TypeCacheDelta) i.next();
							if (delta.getJob() != null && delta.getJob().equals(deltaJob)) {
								if (jobFinished) {
									i.remove();
								} else {
									delta.assignToJob(null);
								}
							}
						}
					}
				}
			}
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void sleeping(IJobChangeEvent event) {
		}
	};
	
	public TypeCache(IProject project, IWorkingCopyProvider workingCopyProvider) {
		fProject = project;
		fWorkingCopyProvider = workingCopyProvider;
		fDeltas.add(new TypeCacheDelta(fProject));
	}

	public boolean contains(ISchedulingRule rule) {
		if (this == rule)
			return true;
		if (rule instanceof ITypeCache) {
			ITypeCache typeCache = (ITypeCache) rule;
			if (fProject.equals(typeCache.getProject()))
				return true;
		}
		return false;
	}

	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof ITypeCache) {
			ITypeCache typeCache = (ITypeCache) rule;
			if (fProject.equals(typeCache.getProject()))
				return true;
		}
		return false;
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	public synchronized boolean isEmpty() {
		return fTypeNameMap.isEmpty();
	}

	public synchronized void insert(ITypeInfo info) {
		// check if enclosing types are already in cache
		IQualifiedTypeName typeName = info.getQualifiedTypeName().getEnclosingTypeName();
		if (typeName != null) {
			while (!typeName.isEmpty()) {
				boolean foundType = false;
				Collection typeCollection = (Collection) fTypeNameMap.get(typeName.getName());
				if (typeCollection == null) {
					typeCollection = new HashSet();
					fTypeNameMap.put(typeName.getName(), typeCollection);
				} else {
					for (Iterator typeIter = typeCollection.iterator(); typeIter.hasNext(); ) {
						ITypeInfo curr = (ITypeInfo) typeIter.next();
						if (curr.getQualifiedTypeName().equals(typeName)) {
							foundType = true;
							break;
						}
					}
				}
				if (!foundType) {
					// create a dummy type to take this place (type 0 == unknown)
					ITypeInfo dummyType = new TypeInfo(0, typeName, this);
					typeCollection.add(dummyType);
				}
				typeName = typeName.removeLastSegments(1);
			}
		}
		
		Collection typeCollection = (Collection) fTypeNameMap.get(info.getName());
		if (typeCollection == null) {
			typeCollection = new HashSet();
			fTypeNameMap.put(info.getName(), typeCollection);
		}
		typeCollection.add(info);
	}
	
	public synchronized void remove(ITypeInfo info) {
		Collection typeCollection = (Collection) fTypeNameMap.get(info.getName());
		if (typeCollection != null) {
			typeCollection.remove(info);
		}
	}

	public synchronized void flush(ITypeSearchScope scope) {
		if (scope.encloses(fProject)) {
			flushAll();
		} else {
			for (Iterator mapIter = fTypeNameMap.entrySet().iterator(); mapIter.hasNext(); ) {
				Map.Entry entry = (Map.Entry) mapIter.next();
				Collection typeCollection = (Collection) entry.getValue();
				for (Iterator typeIter = typeCollection.iterator(); typeIter.hasNext(); ) {
					ITypeInfo info = (ITypeInfo) typeIter.next();
					if (info.isEnclosed(scope)) {
						typeIter.remove();
					}
				}
				if (typeCollection.isEmpty())
					mapIter.remove();
			}
		}
	}
	
	public synchronized void flush(IPath path) {
		ITypeSearchScope scope = new TypeSearchScope();
		scope.add(path, false, null);
		flush(scope);
	}

	public synchronized void flushAll() {
		// flush the entire cache
		fTypeNameMap.clear();
	}

	public synchronized void accept(ITypeInfoVisitor visitor) {
		for (Iterator mapIter = fTypeNameMap.entrySet().iterator(); mapIter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) mapIter.next();
			Collection typeCollection = (Collection) entry.getValue();
			for (Iterator typeIter = typeCollection.iterator(); typeIter.hasNext(); ) {
				ITypeInfo info = (ITypeInfo) typeIter.next();
				visitor.visit(info);
			}
		}
	}

	public synchronized IPath[] getPaths(ITypeSearchScope scope) {
		final Set fPathSet = new HashSet();
		final ITypeSearchScope fScope = scope;
		accept(new ITypeInfoVisitor() {
			public void visit(ITypeInfo info) {
				if (fScope == null || info.isEnclosed(fScope)) {
					ITypeReference[] refs = info.getReferences();
					for (int i = 0; i < refs.length; ++i) {
						IPath path = refs[i].getPath();
						if (fScope == null || fScope.encloses(path))
							fPathSet.add(path);
					}
				}
			}
		});
		return (IPath[]) fPathSet.toArray(new IPath[fPathSet.size()]);
	}

	public synchronized ITypeInfo[] getTypes(ITypeSearchScope scope) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		accept(new ITypeInfoVisitor() {
			public void visit(ITypeInfo info) {
				if (fScope == null || info.isEnclosed(fScope)) {
					fTypesFound.add(info);
				}
			}
		});
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	public synchronized ITypeInfo[] getTypes(IQualifiedTypeName qualifiedName) {
		Collection results = new ArrayList();
		Collection typeCollection = (Collection) fTypeNameMap.get(qualifiedName.getName());
		if (typeCollection != null) {
			for (Iterator typeIter = typeCollection.iterator(); typeIter.hasNext(); ) {
				ITypeInfo info = (ITypeInfo) typeIter.next();
				if (info.getQualifiedTypeName().equals(qualifiedName)) {
					results.add(info);
				}
			}
		}
		return (ITypeInfo[]) results.toArray(new ITypeInfo[results.size()]);
	}
	
	public synchronized ITypeInfo getType(int type, IQualifiedTypeName qualifiedName) {
		Collection typeCollection = (Collection) fTypeNameMap.get(qualifiedName.getName());
		if (typeCollection != null) {
			for (Iterator typeIter = typeCollection.iterator(); typeIter.hasNext(); ) {
				ITypeInfo info = (ITypeInfo) typeIter.next();
				if (info.getQualifiedTypeName().equals(qualifiedName)
						&& (info.getCElementType() == type || info.getCElementType() == 0)) {
					return info;
				}
			}
		}
		return null;
	}
	
	public boolean isUpToDate() {
		synchronized(fDeltas) {
			return fDeltas.isEmpty();
		}
	}
	
	public void addDelta(TypeCacheDelta delta) {
		synchronized(fDeltas) {
			fDeltas.add(delta);
		}
	}
	
	public void reconcile(boolean enableIndexing, int priority, int delay) {
		// check if anything needs to be done
		if (deltasRemaining() == 0)
			return;	// nothing to do

		// cancel any scheduled or running jobs for this project
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(TypeCacherJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeCacherJob deltaJob = (TypeCacherJob) jobs[i];
			if (deltaJob.getCache().equals(this)) {
				deltaJob.cancel();
			}
		}
		
		// check again, in case some jobs finished in the meantime
		if (deltasRemaining() == 0)
			return;	// nothing to do

		TypeCacherJob deltaJob;
		synchronized(fDeltas) {
			// grab all the remaining deltas
			TypeCacheDelta[] jobDeltas = (TypeCacheDelta[]) fDeltas.toArray(new TypeCacheDelta[fDeltas.size()]);

			// create a new job
			deltaJob = new TypeCacherJob(this, jobDeltas, enableIndexing);
			// assign deltas to job
			if (jobDeltas != null) {
				for (int i = 0; i < jobDeltas.length; ++i) {
					jobDeltas[i].assignToJob(deltaJob);
				}
			}
		}

		// schedule the new job
		deltaJob.addJobChangeListener(fJobChangeListener);
		deltaJob.setPriority(priority);
		deltaJob.schedule(delay);
	}

	public void reconcileAndWait(boolean enableIndexing, int priority, IProgressMonitor monitor) {
		reconcile(enableIndexing, priority, 0);
		
		// wait for jobs to complete
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(TypeCacherJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeCacherJob deltaJob = (TypeCacherJob) jobs[i];
			if (deltaJob.getCache().equals(this)) {
				try {
					deltaJob.join(monitor);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	// returns the number of deltas either not assigned to a job,
	// or assigned to a job which is not yet running
	private int deltasRemaining() {
		// count the left-over deltas
		synchronized(fDeltas) {
			int count = 0;
			for (Iterator i = fDeltas.iterator(); i.hasNext(); ) {
				TypeCacheDelta delta = (TypeCacheDelta) i.next();
				TypeCacherJob job = delta.getJob();
				if (job == null || !job.isRunning()) {
					++count;
				}
			}
			return count;
		}
	}
	
	public void cancelJobs() {
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(TypeCacherJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeCacherJob deltaJob = (TypeCacherJob) jobs[i];
			if (deltaJob.getCache().equals(this)) {
				deltaJob.cancel();
			}
		}
		jobs = jobManager.find(TypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeLocatorJob locatorJob = (TypeLocatorJob) jobs[i];
			if (locatorJob.getType().getEnclosingProject().equals(fProject)) {
				locatorJob.cancel();
			}
		}
	}
	
	public void locateType(ITypeInfo info, int priority, int delay) {
		ITypeReference location = info.getResolvedReference();
		if (location != null)
			return;	// nothing to do

		// cancel any scheduled or running jobs for this type
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(TypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeLocatorJob locatorJob = (TypeLocatorJob) jobs[i];
			if (locatorJob.getType().equals(info)) {
				locatorJob.cancel();
			}
		}
		
		// check again, in case some jobs finished in the meantime
		location = info.getResolvedReference();
		if (location != null)
			return;	// nothing to do
		
		// create a new job
		TypeLocatorJob locatorJob = new TypeLocatorJob(info, this, fWorkingCopyProvider);
		// schedule the new job
		locatorJob.setPriority(priority);
		locatorJob.schedule(delay);
	}
	
	public ITypeReference locateTypeAndWait(ITypeInfo info, int priority, IProgressMonitor monitor) {
		locateType(info, priority, 0);

		// wait for jobs to complete
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(TypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			TypeLocatorJob locatorJob = (TypeLocatorJob) jobs[i];
			if (locatorJob.getType().equals(info)) {
				try {
					locatorJob.join(monitor);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return info.getResolvedReference();
	}
}
