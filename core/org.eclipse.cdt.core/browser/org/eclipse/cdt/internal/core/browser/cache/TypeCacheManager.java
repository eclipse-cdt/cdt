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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeCacheChangedListener;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.IWorkingCopyProvider;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

public class TypeCacheManager implements ITypeCacheChangedListener, IndexManager.IIndexerSelectionListener {
	private static final TypeCacheManager fgInstance = new TypeCacheManager();
	private Map fCacheMap;
	private IWorkingCopyProvider fWorkingCopyProvider;
	private ArrayList fChangeListeners = new ArrayList();
	
    private static final int INITIAL_TYPE_MAP_SIZE = 50;
    //TODO make this a WeakHashMap or LRUCache
    private Map fTypeToElementMap = new HashMap(INITIAL_TYPE_MAP_SIZE);
    private Map fElementToTypeMap = new HashMap(INITIAL_TYPE_MAP_SIZE);
    private boolean processTypeCacheEvents = true;
    
	private TypeCacheManager() {
		fCacheMap = new HashMap();
        CModelManager.getDefault().getIndexManager().subscribeForIndexerChangeNotifications( this );
	}
	
	public static TypeCacheManager getInstance() {
		return fgInstance;
	}
    
    protected void finalize() throws Throwable {
        CModelManager.getDefault().getIndexManager().unSubscribeForIndexerChangeNotifications( this );
        super.finalize();
    }
	
	public void setWorkingCopyProvider(IWorkingCopyProvider workingCopyProvider) {
		fWorkingCopyProvider = workingCopyProvider;
	}

	public synchronized void updateProject(IProject project) {
		// TODO finer-grained flush needed, for now just flush the whole map
	    fTypeToElementMap.clear();
	    fElementToTypeMap.clear();
	    addCacheDelta(project, null);
	}

	public synchronized void processElementChanged(ElementChangedEvent event, boolean enableIndexing) {
	    int deltaCount = processDelta(event.getDelta());
	    if (deltaCount > 0) {
			// TODO finer-grained flush needed, for now just flush the whole map
		    fTypeToElementMap.clear();
		    fElementToTypeMap.clear();
	        reconcile(enableIndexing, Job.BUILD, 0);
	    }
	}
	
	private static final int PATH_ENTRY_FLAGS = ICElementDelta.F_ADDED_PATHENTRY_SOURCE
		| ICElementDelta.F_REMOVED_PATHENTRY_SOURCE
		| ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE
		| ICElementDelta.F_CHANGED_PATHENTRY_MACRO
		| ICElementDelta.F_PATHENTRY_REORDER;

	private int processDelta(ICElementDelta delta) {
		ICElement elem = delta.getElement();
		boolean added = (delta.getKind() == ICElementDelta.ADDED);
		boolean removed = (delta.getKind() == ICElementDelta.REMOVED);
		boolean contentChanged = ((delta.getFlags() & ICElementDelta.F_CONTENT) != 0);
		boolean pathEntryChanged = ((delta.getFlags() & PATH_ENTRY_FLAGS) != 0);
		boolean openedOrClosed = (((delta.getFlags() & ICElementDelta.F_CLOSED) != 0) || ((delta.getFlags() & ICElementDelta.F_OPENED) != 0));
		boolean hasChildren = ((delta.getFlags() & ICElementDelta.F_CHILDREN) != 0);
		int deltaCount = 0;
		

		switch (elem.getElementType()) {
			case ICElement.C_PROJECT:
			case ICElement.C_CCONTAINER: {
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				if (added || removed || pathEntryChanged || openedOrClosed) {
					addCacheDelta(project, delta);
					++deltaCount;
				}
			}
			break;
			
			case ICElement.C_UNIT: {
				ICProject cProject = elem.getCProject();
				IProject project = cProject.getProject();
				ITranslationUnit unit = (ITranslationUnit) elem;
				if (unit.isWorkingCopy()) {
					deltaCount += processWorkingCopyDelta(delta);
					return deltaCount;
				}
				if (added || removed || pathEntryChanged || contentChanged) {
					addCacheDelta(project, delta);
					++deltaCount;
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
					addCacheDelta(project, delta);
					++deltaCount;
				}
			}
			break;
		}

		if (hasChildren) {
			ICElementDelta[] children = delta.getAffectedChildren();
			if (children != null) {
				for (int i = 0; i < children.length; ++i) {
				    deltaCount += processDelta(children[i]);
				}
			}
		}

		return deltaCount;
	}
	
	private void addCacheDelta(IProject project, ICElementDelta delta) {
	    if (delta == null) {
	        getCache(project).addDelta(new TypeCacheDelta(project));
	    } else {
	        getCache(project).addDelta(new TypeCacheDelta(project, delta));
	    }
	}

	private int processWorkingCopyDelta(ICElementDelta delta) {
		// ignore workies copies for now
		return 0;
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
				cache = new TypeCache(project, fWorkingCopyProvider, this);
				fCacheMap.put(project, cache);
			}
			return cache;
		}
	}
	
	public synchronized void reconcile(boolean enableIndexing, int priority, int delay) {
		if (!(processTypeCacheEvents))
			return;
		
		TypeSearchScope workspaceScope = new TypeSearchScope(true);
		IProject[] projects = workspaceScope.getEnclosingProjects();
		for (int i = 0; i < projects.length; ++i) {
			ITypeCache cache = getCache(projects[i]);
			cache.reconcile(enableIndexing, priority, delay);
		}
	}

	public synchronized void reconcileAndWait(boolean enableIndexing, int priority, IProgressMonitor monitor) {
		if (!(processTypeCacheEvents))
			return;
		
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
	
	public void updateCache(ITypeSearchScope scope, IProgressMonitor monitor) {
		// schedule jobs to update cache
		IProject[] projects = scope.getEnclosingProjects();
		monitor.beginTask(TypeCacheMessages.getString("AllTypesCache.updateCache.taskName"), projects.length); //$NON-NLS-1$
		for (int i = 0; i < projects.length; ++i) {
			IProject project = projects[i];
			// wait for any running jobs to finish
			getCache(project).reconcileAndWait(true, Job.SHORT, monitor);
		}
		monitor.done();
	}
	
	/**
	 * Resolves a type location.
	 * 
	 * @param info the type to search for
	 * @param monitor the progress monitor
	 */
	public ITypeReference resolveTypeLocation(ITypeInfo info, IProgressMonitor monitor, boolean enableIndexing) {
		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			// cancel background jobs
			IProject project = info.getEnclosingProject();
			ITypeCache cache = getCache(project);
			cache.cancelJobs();

			// start the search job
			cache.locateTypeAndWait(info, Job.SHORT, monitor);

			// get the newly parsed location
			location = info.getResolvedReference();

			// resume background jobs
			reconcile(enableIndexing, Job.BUILD, 0);
		}
		return location;
	}
	
    public void addTypeCacheChangedListener(ITypeCacheChangedListener listener) {
    	// add listener only if it is not already present
        synchronized(fChangeListeners) {
            if (!fChangeListeners.contains(listener)) {
	    	    fChangeListeners.add(listener);
            }
        }
    }

    public void removeTypeCacheChangedListener(ITypeCacheChangedListener listener) {
		synchronized(fChangeListeners) {
            fChangeListeners.remove(listener);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.ITypeCacheChangedListener#typeCacheChanged(org.eclipse.core.resources.IProject)
     */
    public synchronized void typeCacheChanged(final IProject project) {
    	// clone so that a listener cannot have a side-effect on this list when being notified
		ArrayList listeners;
        synchronized(fChangeListeners) {
            listeners = (ArrayList) fChangeListeners.clone();
        }
		for (Iterator i = listeners.iterator(); i.hasNext(); ) {
		    final ITypeCacheChangedListener listener = (ITypeCacheChangedListener) i.next();
    		Platform.run(new ISafeRunnable() {
    			public void handleException(Throwable e) {
    				IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR, "Exception occurred in listener of type cache change notification", e); //$NON-NLS-1$
    			    CCorePlugin.log(status);
    			}
    			public void run() throws Exception {
    				listener.typeCacheChanged(project);
    			}
    		});
    	}
    }
    
    public ITypeInfo getTypeForElement(ICElement element, boolean forceUpdate, boolean forceResolve, boolean enableIndexing, IProgressMonitor monitor) {
        if (element.exists()) {
	        ITypeInfo cachedInfo = (ITypeInfo) fElementToTypeMap.get(element);
	        if (cachedInfo != null && cachedInfo.exists())
	            return cachedInfo;
        }
        
		IQualifiedTypeName qualifiedName = TypeUtil.getFullyQualifiedName(element);
		if (qualifiedName != null) {
			ICProject cProject = element.getCProject();
			IProject project = cProject.getProject();
		    ITypeCache cache = getCache(project);
			if (!cache.isUpToDate() && forceUpdate) {
		        if (monitor == null)
		            monitor = new NullProgressMonitor();
				// wait for any running jobs to finish
				cache.reconcileAndWait(true, Job.SHORT, monitor);
			}
		    
		    ITypeInfo info = cache.getType(element.getElementType(), qualifiedName);
		    if (info != null) {
				ITypeReference ref = info.getResolvedReference();
				if (ref == null && forceResolve) {
			        if (monitor == null)
			            monitor = new NullProgressMonitor();
				    ref = resolveTypeLocation(info, monitor, enableIndexing);
				}
				
				// cache for later use
				fElementToTypeMap.put(element, info);
				return info;
		    }
		}
		return null;
    }

    public ICElement getElementForType(ITypeInfo type, boolean forceUpdate, boolean forceResolve, boolean enableIndexing, IProgressMonitor monitor) {
        if (type.exists()) {
            ICElement cachedElem = (ICElement) fTypeToElementMap.get(type);
	        if (cachedElem != null && cachedElem.exists())
	            return cachedElem;
        }
        
        IProject project = type.getEnclosingProject();
	    ITypeCache cache = getCache(project);
		if (!cache.isUpToDate() && forceUpdate) {
	        if (monitor == null)
	            monitor = new NullProgressMonitor();
			// wait for any running jobs to finish
			cache.reconcileAndWait(true, Job.SHORT, monitor);
			
			//TODO replace type with new type from cache???
		}
        
		ITypeReference ref = type.getResolvedReference();
		if (ref == null && forceResolve) {
		    ref = resolveTypeLocation(type, monitor, enableIndexing);
		}
		if (ref != null) {
			ICElement[] elems = ref.getCElements();
			if (elems != null && elems.length > 0) {
			    ICElement foundElem = elems[0];
				if (elems.length > 1) {
					for (int i = 0; i < elems.length; ++i) {
						ICElement elem = elems[i];
						if (elem.getElementType() == type.getCElementType() && elem.getElementName().equals(type.getName())) {
							//TODO should check fully qualified name
						    foundElem = elem;
						    break;
						}
					}
				}

				if (foundElem != null) {
					// cache for later use
				    fTypeToElementMap.put(type, foundElem);
					return foundElem;
				}
			}
		}
		return null;
    }
	public boolean getProcessTypeCacheEvents() {
		return processTypeCacheEvents;
	}
	public void setProcessTypeCacheEvents(boolean processTypeCacheEvents) {
		this.processTypeCacheEvents = processTypeCacheEvents;
	}

    public void indexerSelectionChanged(IProject project) {
        addCacheDelta(project, null );
    }
}
