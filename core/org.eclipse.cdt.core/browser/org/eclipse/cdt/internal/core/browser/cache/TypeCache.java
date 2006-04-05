/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.cdt.core.browser.ITypeCacheChangedListener;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeInfoVisitor;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopyProvider;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
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
	private final Map fTypeKeyMap = new HashMap(INITIAL_TYPE_COUNT);
	final IProject fProject;
	private final IWorkingCopyProvider fWorkingCopyProvider;
	final Collection fDeltas = new ArrayList();
	final ITypeInfo fGlobalNamespace;
	private final Map fTypeToSubTypes = new HashMap();
	private final Map fTypeToSuperTypes = new HashMap();
	ITypeCacheChangedListener fChangeListener = null;

	private static final class SuperTypeEntry {
		ITypeInfo superType;
		ASTAccessVisibility access;
		boolean isVirtual;
		SuperTypeEntry(ITypeInfo superType, ASTAccessVisibility access, boolean isVirtual) {
			this.superType = superType;
			this.access = access;
			this.isVirtual = isVirtual;
		}
	}
	
	private SuperTypeEntry findSuperTypeEntry(Collection entryCollection, ITypeInfo superType) {
		for (Iterator i = entryCollection.iterator(); i.hasNext(); ) {
			SuperTypeEntry e = (SuperTypeEntry) i.next();
			if (e.superType.equals(superType)) {
				return e;
			}
		}
		return null;
	}
	
	private static final int[] ENCLOSING_TYPES = {ICElement.C_NAMESPACE, ICElement.C_CLASS, ICElement.C_STRUCT, 0};
	
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
					boolean jobFinished = (status.equals(Status.OK_STATUS)
						&& !deltaJob.isIndexerBusy());
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
				// TODO finer-grained change deltas
				if (fChangeListener != null)
				    fChangeListener.typeCacheChanged(fProject);
			}
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void sleeping(IJobChangeEvent event) {
		}
	};
	
	private static class GlobalNamespace implements IQualifiedTypeName {
		
		private static final String GLOBAL_NAMESPACE = TypeCacheMessages.getString("TypeCache.globalNamespace"); //$NON-NLS-1$
		private static final String[] segments = new String[] { GLOBAL_NAMESPACE };

		public GlobalNamespace() {
		}
		
		public String getName() {
			return GLOBAL_NAMESPACE;
		}
		
		public String[] getEnclosingNames() {
			return null;
		}
		
		public String getFullyQualifiedName() {
			return GLOBAL_NAMESPACE;
		}
		
		public IQualifiedTypeName getEnclosingTypeName() {
			return null;
		}
		
		public boolean isEmpty() {
			return false;
		}
		
		public boolean isGlobal() {
			return true;
		}

		public boolean isQualified() {
		    return false;
		}
		
		public boolean isValidSegment(String segment) {
		    return false;
		}
		
		public int segmentCount() {
			return 1;
		}
		
		public String[] segments() {
			return segments;
		}
		
		public String segment(int index) {
			if (index > 0)
				return null;
			return GLOBAL_NAMESPACE;
		}
		
		public String lastSegment() {
			return GLOBAL_NAMESPACE;
		}
		
		public int matchingFirstSegments(IQualifiedTypeName typeName) {
			return 1;
		}
		
		public boolean isPrefixOf(IQualifiedTypeName typeName) {
			return true;
		}
		
		public IQualifiedTypeName append(String[] names) {
			return new QualifiedTypeName(names);
		}
		
		public IQualifiedTypeName append(IQualifiedTypeName typeName) {
			return new QualifiedTypeName(typeName);
		}
		
		public IQualifiedTypeName append(String qualifiedName) {
			return new QualifiedTypeName(qualifiedName);
		}
		
		public IQualifiedTypeName removeFirstSegments(int count) {
			return this;
		}
		
		public IQualifiedTypeName removeLastSegments(int count) {
			return this;
		}
		
		public boolean isLowLevel() {
			return false;
		}
		
		public boolean isValid() {
			return true;
		}
		
		public int hashCode() {
			return GLOBAL_NAMESPACE.hashCode();
		}
		
		public String toString() {
			return getFullyQualifiedName();
		}
		
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof IQualifiedTypeName)) {
				return false;
			}
			return equals((IQualifiedTypeName)obj);
		}
		
		public int compareTo(Object obj) {
			if (obj == this) {
				return 0;
			}
			if (!(obj instanceof IQualifiedTypeName)) {
				throw new ClassCastException();
			}
			return compareTo((IQualifiedTypeName)obj);
		}

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.browser.IQualifiedTypeName#equals(org.eclipse.cdt.core.browser.IQualifiedTypeName)
         */
        public boolean equals(IQualifiedTypeName typeName) {
			return (typeName instanceof GlobalNamespace);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.browser.IQualifiedTypeName#equalsIgnoreCase(org.eclipse.cdt.core.browser.IQualifiedTypeName)
         */
        public boolean equalsIgnoreCase(IQualifiedTypeName typeName) {
			return (typeName instanceof GlobalNamespace);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.browser.IQualifiedTypeName#compareTo(org.eclipse.cdt.core.browser.IQualifiedTypeName)
         */
        public int compareTo(IQualifiedTypeName typeName) {
			return getFullyQualifiedName().compareTo(typeName.getFullyQualifiedName());
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.browser.IQualifiedTypeName#compareToIgnoreCase(org.eclipse.cdt.core.browser.IQualifiedTypeName)
         */
        public int compareToIgnoreCase(IQualifiedTypeName typeName) {
			return getFullyQualifiedName().compareToIgnoreCase(typeName.getFullyQualifiedName());
        }
	}
	
	private static class HashKey {
		private IQualifiedTypeName name;
		private int type;
		public HashKey(IQualifiedTypeName name, int type) {
			this.name = name;
			this.type = type;
		}
		public int hashCode() {
			return (this.name.hashCode() + this.type);
		}
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof HashKey)) {
				return false;
			}
			HashKey otherKey = (HashKey)obj;
			return (this.type == otherKey.type && this.name.equals(otherKey.name));
		}
	}
	
	public TypeCache(IProject project, IWorkingCopyProvider workingCopyProvider) {
		fProject = project;
		fWorkingCopyProvider = workingCopyProvider;
		fDeltas.add(new TypeCacheDelta(fProject));
		fGlobalNamespace = new TypeInfo(ICElement.C_NAMESPACE, new GlobalNamespace());
		fGlobalNamespace.setCache(this);
	}

	public TypeCache(IProject project, IWorkingCopyProvider workingCopyProvider, ITypeCacheChangedListener listener) {
	    this(project, workingCopyProvider);
	    fChangeListener = listener;
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
		return fTypeKeyMap.isEmpty();
	}
	
	public synchronized void insert(ITypeInfo newType) {
		// check if enclosing types are already in cache
		IQualifiedTypeName enclosingName = newType.getQualifiedTypeName().getEnclosingTypeName();
		if (enclosingName != null) {
			while (!enclosingName.isEmpty()) {
				// try namespace, class, struct, then undefined
				ITypeInfo enclosingType = null;
				for (int i = 0; enclosingType == null && i < ENCLOSING_TYPES.length; ++i) {
					enclosingType = (ITypeInfo) fTypeKeyMap.get(new HashKey(enclosingName, ENCLOSING_TYPES[i]));
				}
				if (enclosingType == null) {
					// create a dummy type to take this place (type 0 == unknown)
					ITypeInfo dummyType = new TypeInfo(0, enclosingName);
					dummyType.setCache(this);
					fTypeKeyMap.put(new HashKey(enclosingName, 0), dummyType);
				}
				enclosingName = enclosingName.removeLastSegments(1);
			}
		}
		
		fTypeKeyMap.put(new HashKey(newType.getQualifiedTypeName(), newType.getCElementType()), newType);
		newType.setCache(this);
	}
	
	public synchronized void remove(ITypeInfo info) {
		fTypeKeyMap.remove(new HashKey(info.getQualifiedTypeName(), info.getCElementType()));
		info.setCache(null);
	}

	public synchronized void flush(ITypeSearchScope scope) {
		if (scope.encloses(fProject)) {
			flushAll();
		} else {
			for (Iterator mapIter = fTypeKeyMap.entrySet().iterator(); mapIter.hasNext(); ) {
				Map.Entry entry = (Map.Entry) mapIter.next();
				ITypeInfo info = (ITypeInfo) entry.getValue();
				if (info.isEnclosed(scope)) {
					mapIter.remove();
				}
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
		accept(new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				info.setCache(null);
				return true;
			}
			public boolean shouldContinue() { return true; }
		});
		fTypeKeyMap.clear();
	}

	public synchronized void addSupertype(ITypeInfo type, ITypeInfo supertype, ASTAccessVisibility access, boolean isVirtual) {
		Collection entryCollection = (Collection) fTypeToSuperTypes.get(type);
		if (entryCollection == null) {
			entryCollection = new ArrayList();
			fTypeToSuperTypes.put(type, entryCollection);
		}
		if (findSuperTypeEntry(entryCollection, supertype) == null) {
			entryCollection.add(new SuperTypeEntry(supertype, access, isVirtual));
			supertype.setCache(this);
		}
	}

	public synchronized ITypeInfo[] getSupertypes(ITypeInfo type) {
		Collection entryCollection = (Collection) fTypeToSuperTypes.get(type);
		if (entryCollection != null && !entryCollection.isEmpty()) {
			ITypeInfo[] superTypes = new ITypeInfo[entryCollection.size()];
			int count = 0;
			for (Iterator i = entryCollection.iterator(); i.hasNext(); ) {
				SuperTypeEntry e = (SuperTypeEntry) i.next();
				superTypes[count++] = e.superType;
			}
			return superTypes;
		}
		return null;
	}

	public ASTAccessVisibility getSupertypeAccess(ITypeInfo type, ITypeInfo superType) {
		Collection entryCollection = (Collection) fTypeToSuperTypes.get(type);
		if (entryCollection != null && !entryCollection.isEmpty()) {
			SuperTypeEntry e = findSuperTypeEntry(entryCollection, superType);
			if (e != null)
				return e.access;
		}
		return null;
	}
	
	public synchronized void addSubtype(ITypeInfo type, ITypeInfo subtype) {
		Collection typeCollection = (Collection) fTypeToSubTypes.get(type);
		if (typeCollection == null) {
			typeCollection = new ArrayList();
			fTypeToSubTypes.put(type, typeCollection);
		}
		if (!typeCollection.contains(subtype)) {
			typeCollection.add(subtype);
			subtype.setCache(this);
		}
	}
	
	public synchronized ITypeInfo[] getSubtypes(ITypeInfo type) {
		Collection typeCollection = (Collection) fTypeToSubTypes.get(type);
		if (typeCollection != null && !typeCollection.isEmpty()) {
			return (ITypeInfo[]) typeCollection.toArray(new ITypeInfo[typeCollection.size()]);
		}
		return null;
	}

	public synchronized void accept(ITypeInfoVisitor visitor) {
		for (Iterator mapIter = fTypeKeyMap.entrySet().iterator(); mapIter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) mapIter.next();
			ITypeInfo info = (ITypeInfo) entry.getValue();
			if (!visitor.shouldContinue())
				return; // stop visiting
			visitor.visit(info);
		}
	}

	public synchronized IPath[] getPaths(final ITypeSearchScope scope) {
		final Set pathSet = new HashSet();
		accept(new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (scope == null || info.isEnclosed(scope)) {
					ITypeReference[] refs = info.getReferences();
					if (refs != null) {
						for (int i = 0; i < refs.length; ++i) {
							IPath path = refs[i].getPath();
							if (scope == null || scope.encloses(path))
								pathSet.add(path);
						}
					}
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		});
		return (IPath[]) pathSet.toArray(new IPath[pathSet.size()]);
	}

	public synchronized ITypeInfo[] getTypes(final ITypeSearchScope scope) {
		final Collection results = new ArrayList();
		accept(new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (scope == null || info.isEnclosed(scope)) {
					results.add(info);
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		});
		return (ITypeInfo[]) results.toArray(new ITypeInfo[results.size()]);
	}
	
	public synchronized ITypeInfo[] getTypes(IQualifiedTypeName qualifiedName, boolean matchEnclosed, boolean ignoreCase) {
		Collection results = new ArrayList();
		if (!ignoreCase && !matchEnclosed) {
			for (int i = 0; i < ITypeInfo.KNOWN_TYPES.length; ++i) {
				ITypeInfo info = (ITypeInfo) fTypeKeyMap.get(new HashKey(qualifiedName, ITypeInfo.KNOWN_TYPES[i]));
				if (info != null) {
					results.add(info);
				}
			}
			ITypeInfo info = (ITypeInfo) fTypeKeyMap.get(new HashKey(qualifiedName, 0));
			if (info != null) {
				results.add(info);
			}
		} else {
		    // TODO this should probably use a more efficient search algorithm
		    for (Iterator mapIter = fTypeKeyMap.entrySet().iterator(); mapIter.hasNext(); ) {
				Map.Entry entry = (Map.Entry) mapIter.next();
				ITypeInfo info = (ITypeInfo) entry.getValue();
				IQualifiedTypeName currName = info.getQualifiedTypeName();
				
				if (ignoreCase) {
					if (matchEnclosed && currName.segmentCount() > qualifiedName.segmentCount()
					        && currName.lastSegment().equalsIgnoreCase(qualifiedName.lastSegment())) {
						currName = currName.removeFirstSegments(currName.segmentCount() - qualifiedName.segmentCount());
					}
					if (currName.equalsIgnoreCase(qualifiedName)) {
						results.add(info);
					}
				} else {
					if (matchEnclosed && currName.segmentCount() > qualifiedName.segmentCount()
					        && currName.lastSegment().equals(qualifiedName.lastSegment())) {
						currName = currName.removeFirstSegments(currName.segmentCount() - qualifiedName.segmentCount());
					}
					if (currName.equals(qualifiedName)) {
						results.add(info);
					}
				}
			}
		}
		return (ITypeInfo[]) results.toArray(new ITypeInfo[results.size()]);
	}
	
	public synchronized ITypeInfo getType(int type, IQualifiedTypeName qualifiedName) {
		ITypeInfo info = (ITypeInfo) fTypeKeyMap.get(new HashKey(qualifiedName, type));
		if (info == null && type != 0) {
			info = (ITypeInfo) fTypeKeyMap.get(new HashKey(qualifiedName, 0));			
		}
		return info;
	}
	
	public synchronized ITypeInfo getEnclosingType(ITypeInfo info, final int[] kinds) {
		IQualifiedTypeName enclosingName = info.getQualifiedTypeName().getEnclosingTypeName();
		if (enclosingName != null) {
			// try namespace, class, struct, then undefined
			ITypeInfo enclosingType = null;
			for (int i = 0; enclosingType == null && i < ENCLOSING_TYPES.length; ++i) {
				if (ArrayUtil.contains(kinds, ENCLOSING_TYPES[i])) {
					enclosingType = (ITypeInfo) fTypeKeyMap.get(new HashKey(enclosingName, ENCLOSING_TYPES[i]));
				}
			}
			return enclosingType;
		}
		return null;
	}
		
	public synchronized ITypeInfo getEnclosingNamespace(ITypeInfo info, boolean includeGlobalNamespace) {
		IQualifiedTypeName enclosingName = info.getQualifiedTypeName().getEnclosingTypeName();
		if (enclosingName != null) {
		    // look for namespace
			ITypeInfo enclosingType = (ITypeInfo) fTypeKeyMap.get(new HashKey(enclosingName, ICElement.C_NAMESPACE));
			if (enclosingType != null) {
			    return enclosingType;
			}
			// try class, struct, then undefined
			final int[] kinds = {ICElement.C_CLASS, ICElement.C_STRUCT, 0};
			for (int i = 0; enclosingType == null && i < kinds.length; ++i) {
				enclosingType = (ITypeInfo) fTypeKeyMap.get(new HashKey(enclosingName, kinds[i]));
			}
			if (enclosingType != null) {
			    return getEnclosingNamespace(enclosingType, includeGlobalNamespace);
			}
		}
		if (includeGlobalNamespace)
			return fGlobalNamespace;
		return null;
	}

	public synchronized ITypeInfo getRootNamespace(ITypeInfo info, boolean includeGlobalNamespace) {
		IQualifiedTypeName qualifiedName = info.getQualifiedTypeName();
		if (qualifiedName.isGlobal()) {
			if (info.getCElementType() == ICElement.C_NAMESPACE)
				return info;
			if (includeGlobalNamespace)
				return fGlobalNamespace;
			return null;
		}
		IQualifiedTypeName namespace = qualifiedName.removeLastSegments(qualifiedName.segmentCount()-1);
		// try namespace, then undefined
		ITypeInfo namespaceType = (ITypeInfo) fTypeKeyMap.get(new HashKey(namespace, ICElement.C_NAMESPACE));
		if (namespaceType == null)
			namespaceType = (ITypeInfo) fTypeKeyMap.get(new HashKey(namespace, 0));
		return namespaceType;
	}

	public synchronized boolean hasEnclosedTypes(final ITypeInfo info) {
		final IQualifiedTypeName parentName = info.getQualifiedTypeName();
		final boolean[] foundTypes = { false };
		accept(new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo type) {
				if (type != info && parentName.isPrefixOf(type.getQualifiedTypeName())) {
					foundTypes[0] = true;
				}
				return true;
			}
			public boolean shouldContinue() {
				return !foundTypes[0];
			}
		});
		return foundTypes[0];
	}

	public synchronized ITypeInfo[] getEnclosedTypes(final ITypeInfo enclosedBy, final int kinds[]) {
		final IQualifiedTypeName parentName = enclosedBy.getQualifiedTypeName();
		final Collection results = new ArrayList();
		accept(new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo type) {
				if (ArrayUtil.contains(kinds, type.getCElementType())) {
					IQualifiedTypeName enclosingName = type.getQualifiedTypeName().getEnclosingTypeName();
					if (enclosedBy == fGlobalNamespace) {
						if (enclosingName == null) {
							results.add(type);
						} else {
//							// check if enclosing parent is namespace
//							getRootNamespace(type);
						}
					} else if (parentName.equals(enclosingName)) {
						results.add(type);
					}
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		});

		return (ITypeInfo[]) results.toArray(new ITypeInfo[results.size()]);
	}

	public ITypeInfo getGlobalNamespace() {
		return fGlobalNamespace;
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

		TypeCacherJob deltaJob = null;
		IndexManager indexManager = CModelManager.getDefault().getIndexManager();
		ICDTIndexer indexer = indexManager.getIndexerForProject( fProject );
		boolean haveIndexer = (indexer != null && indexer.isIndexEnabled( fProject ));
		synchronized(fDeltas) {
			if( haveIndexer ){
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
			} else {
				//we don't have an indexer, don't create a job to do these deltas, throw them away
				fDeltas.clear();
			}
		}

		if( deltaJob != null ){
			// schedule the new job
			deltaJob.addJobChangeListener(fJobChangeListener);
			deltaJob.setPriority(priority);
			deltaJob.schedule(delay);
		}
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
		jobs = jobManager.find(SubTypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			SubTypeLocatorJob locatorJob = (SubTypeLocatorJob) jobs[i];
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
	
	public void locateSupertypes(ITypeInfo info, int priority, int delay) {
		ITypeInfo[] superTypes = getSupertypes(info);
		if (superTypes != null)
			return;	// nothing to do
		
		locateType(info, priority, delay);
	}

	public ITypeInfo[] locateSupertypesAndWait(ITypeInfo info, int priority, IProgressMonitor monitor) {
		locateSupertypes(info, priority, 0);

		// wait for jobs to complete
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(SubTypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			SubTypeLocatorJob locatorJob = (SubTypeLocatorJob) jobs[i];
			if (locatorJob.getType().equals(info)) {
				try {
					locatorJob.join(monitor);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return getSupertypes(info);
	}

	public void locateSubtypes(ITypeInfo info, int priority, int delay) {
		ITypeInfo[] subTypes = getSubtypes(info);
		if (subTypes != null)
			return;	// nothing to do

		// cancel any scheduled or running jobs for this type
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(SubTypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			SubTypeLocatorJob locatorJob = (SubTypeLocatorJob) jobs[i];
			if (locatorJob.getType().equals(info)) {
				locatorJob.cancel();
			}
		}
		
		// check again, in case some jobs finished in the meantime
		subTypes = getSubtypes(info);
		if (subTypes != null)
			return;	// nothing to do
		
		// create a new job
		SubTypeLocatorJob locatorJob = new SubTypeLocatorJob(info, this, fWorkingCopyProvider);
		// schedule the new job
		locatorJob.setPriority(priority);
		locatorJob.schedule(delay);
	}

	public ITypeInfo[] locateSubtypesAndWait(ITypeInfo info, int priority, IProgressMonitor monitor) {
		locateSubtypes(info, priority, 0);

		// wait for jobs to complete
		IJobManager jobManager = Platform.getJobManager();
		Job[] jobs = jobManager.find(SubTypeLocatorJob.FAMILY);
		for (int i = 0; i < jobs.length; ++i) {
			SubTypeLocatorJob locatorJob = (SubTypeLocatorJob) jobs[i];
			if (locatorJob.getType().equals(info)) {
				try {
					locatorJob.join(monitor);
				} catch (InterruptedException e) {
				}
			}
		}
		
		return getSubtypes(info);
	}
}
