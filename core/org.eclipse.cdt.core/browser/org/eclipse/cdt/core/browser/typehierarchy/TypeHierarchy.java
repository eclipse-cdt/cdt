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
package org.eclipse.cdt.core.browser.typehierarchy;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

public class TypeHierarchy implements ITypeHierarchy, IElementChangedListener {

	public static boolean DEBUG = false;
    
	private static final class TypeEntry {
		ITypeInfo type;
		ASTAccessVisibility access;
		TypeEntry(ITypeInfo type, ASTAccessVisibility access) {
			this.type = type;
			this.access = access;
		}
	}
	private static final int INITIAL_SUPER_TYPES = 1;
	private static final int INITIAL_SUB_TYPES = 1;
	private static final ITypeInfo[] NO_TYPES = new ITypeInfo[0];
	private ArrayList fRootTypes = new ArrayList();
	private Map fTypeToSuperTypes = new HashMap();
	private Map fTypeToSubTypes = new HashMap();

	private ITypeInfo fFocusType;
	
	/**
	 * The progress monitor to report work completed too.
	 */
	protected IProgressMonitor fProgressMonitor = null;
	/**
	 * Change listeners - null if no one is listening.
	 */
	protected ArrayList fChangeListeners = null;
	
	/*
	 * A map from Openables to ArrayLists of ITypes
	 */
	public Map files = null;
	
	/**
	 * Whether this hierarchy should contains subtypes.
	 */
	protected boolean fComputeSubtypes;

	/**
	 * The scope this hierarchy should restrain itsef in.
	 */
	ICSearchScope fScope;
	
	/*
	 * Whether this hierarchy needs refresh
	 */
	public boolean fNeedsRefresh = true;
//	/*
//	 * Collects changes to types
//	 */
//	protected ChangeCollector fChangeCollector;
	
	
	
	/**
	 * Creates a TypeHierarchy on the given type.
	 */
	public TypeHierarchy(ITypeInfo type) {
		fFocusType = type;
	}
	
	/**
	 * Adds the type to the collection of root classes
	 * if the classes is not already present in the collection.
	 */
	public void addRootType(ITypeInfo type) {
		if (!fRootTypes.contains(type)) {
			fRootTypes.add(type);
		}
	}
	
	/**
	 * Adds the given supertype to the type.
	 */
	public void addSuperType(ITypeInfo type, ITypeInfo superType, ASTAccessVisibility access) {
		Collection superEntries = (Collection) fTypeToSuperTypes.get(type);
		if (superEntries == null) {
		    superEntries = new ArrayList(INITIAL_SUPER_TYPES);
			fTypeToSuperTypes.put(type, superEntries);
		}
		Collection subTypes = (Collection) fTypeToSubTypes.get(superType);
		if (subTypes == null) {
			subTypes = new ArrayList(INITIAL_SUB_TYPES);
			fTypeToSubTypes.put(superType, subTypes);
		}
		if (!subTypes.contains(type)) {
			subTypes.add(type);
		}
		for (Iterator i = superEntries.iterator(); i.hasNext(); ) {
			TypeEntry entry = (TypeEntry)i.next();
			if (entry.type.equals(superType)) {
			    // update the access
			    entry.access = access;
				return;	// don't add if already exists
			}
		}
		TypeEntry typeEntry = new TypeEntry(superType, access);
		superEntries.add(typeEntry);
	}
	
	/**
	 * Adds the given subtype to the type.
	 */
	protected void addSubType(ITypeInfo type, ITypeInfo subType) {
		Collection subTypes = (Collection) fTypeToSubTypes.get(type);
		if (subTypes == null) {
			subTypes = new ArrayList(INITIAL_SUB_TYPES);
			fTypeToSubTypes.put(type, subTypes);
		}
		if (!subTypes.contains(subType)) {
			subTypes.add(subType);
		}

		Collection superEntries = (Collection) fTypeToSuperTypes.get(subType);
		if (superEntries == null) {
		    superEntries = new ArrayList(INITIAL_SUPER_TYPES);
			fTypeToSuperTypes.put(subType, superEntries);
		}
		for (Iterator i = superEntries.iterator(); i.hasNext(); ) {
			TypeEntry entry = (TypeEntry)i.next();
			if (entry.type.equals(type))
				return;	// don't add if already exists
		}
		// default to private access
		TypeEntry typeEntry = new TypeEntry(type, ASTAccessVisibility.PRIVATE);
		superEntries.add(typeEntry);
	}

	/**
	 * Returns true if type already has the given supertype.
	 */
	public boolean hasSuperType(ITypeInfo type, ITypeInfo superType) {
		Collection entries = (Collection) fTypeToSuperTypes.get(type);
		if (entries != null) {
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				TypeEntry entry = (TypeEntry)i.next();
				if (entry.type.equals(superType))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns an array of supertypes for the given type - will never return null.
	 */
	public ITypeInfo[] getSuperTypes(ITypeInfo type) {
		Collection entries = (Collection) fTypeToSuperTypes.get(type);
		if (entries != null) {
			ArrayList superTypes = new ArrayList(INITIAL_SUPER_TYPES);
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				TypeEntry entry = (TypeEntry)i.next();
				superTypes.add(entry.type);
			}
			return (ITypeInfo[])superTypes.toArray(new ITypeInfo[superTypes.size()]);
		}
		return NO_TYPES;
	}
	
	/**
	 * Returns an array of subtypes for the given type - will never return null.
	 */
	public ITypeInfo[] getSubTypes(ITypeInfo type) {
		Collection subTypes = (Collection) fTypeToSubTypes.get(type);
		if (subTypes != null) {
			return (ITypeInfo[])subTypes.toArray(new ITypeInfo[subTypes.size()]);
		}
		return NO_TYPES;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#addTypeHierarchyChangedListener(org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchyChangedListener)
     */
    public void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
    	ArrayList listeners = fChangeListeners;
    	if (listeners == null) {
    		fChangeListeners = listeners = new ArrayList();
    	}
    	
    	// register with JavaCore to get Java element delta on first listener added
    	if (listeners.size() == 0) {
    		CoreModel.getDefault().addElementChangedListener(this);
    	}
    	
    	// add listener only if it is not already present
    	if (listeners.indexOf(listener) == -1) {
    		listeners.add(listener);
    	}
    }


    /**
     * @see ITypeHierarchy
     */
    public synchronized void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener) {
    	ArrayList listeners = fChangeListeners;
    	if (listeners == null) {
    		return;
    	}
    	listeners.remove(listener);

    	// deregister from JavaCore on last listener removed
    	if (listeners.isEmpty()) {
    		CoreModel.getDefault().removeElementChangedListener(this);
    	}
    }
    
    /**
     * Determines if the change effects this hierarchy, and fires
     * change notification if required.
     */
    public void elementChanged(ElementChangedEvent event) {
    	// type hierarchy change has already been fired
    	if (fNeedsRefresh) return;
    	
    	if (isAffected(event.getDelta())) {
    		fNeedsRefresh = true;
    		fireChange();
    	}
    }
    
    /**
     * Returns true if the given delta could change this type hierarchy
     */
    public synchronized boolean isAffected(ICElementDelta delta) {
//    	ICElement element= delta.getElement();
//    	switch (element.getElementType()) {
//    		case ICElement.C_MODEL:
//    			return isAffectedByCModel(delta, element);
//    		case ICElement.C_PROJECT:
//    			return isAffectedByCProject(delta, element);
//    		case ICElement.C_UNIT:
//    			return isAffectedByOpenable(delta, element);
//    	}
//    	return false;
    	return true;
    }
    
    /**
     * Notifies listeners that this hierarchy has changed and needs
     * refreshing. Note that listeners can be removed as we iterate
     * through the list.
     */
    public void fireChange() {
    	ArrayList listeners = fChangeListeners;
    	if (listeners == null) {
    		return;
    	}
    	if (DEBUG) {
    		System.out.println("FIRING hierarchy change ["+Thread.currentThread()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
    		if (fFocusType != null) {
    			System.out.println("    for hierarchy focused on " + fFocusType.toString()); //$NON-NLS-1$
    		}
    	}
    	// clone so that a listener cannot have a side-effect on this list when being notified
    	listeners = (ArrayList)listeners.clone();
    	for (int i= 0; i < listeners.size(); i++) {
    		final ITypeHierarchyChangedListener listener= (ITypeHierarchyChangedListener)listeners.get(i);
    		Platform.run(new ISafeRunnable() {
    			public void handleException(Throwable exception) {
    				Util.log(exception, "Exception occurred in listener of Type hierarchy change notification", ICLogConstants.CDT); //$NON-NLS-1$
    			}
    			public void run() throws Exception {
    				listener.typeHierarchyChanged(TypeHierarchy.this);
    			}
    		});
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#contains(org.eclipse.cdt.core.model.ICElement)
     */
    public boolean contains(ICElement type) {
    	// classes
	    ITypeInfo info = AllTypesCache.getTypeForElement(type);
        
	    if (info == null)
	        return false;
        
    	if (fTypeToSuperTypes.get(info) != null) {
    		return true;
    	}

    	// root classes
    	if (fRootTypes.contains(type)) return true;

        return false;
    }
    
    /**
     * @see ITypeHierarchy
     */
    public boolean exists() {
    	if (!fNeedsRefresh) return true;
    	
    	return (fFocusType == null || fFocusType.exists()) && cProject().exists();
    }
    
    /**
     * Returns the C project this hierarchy was created in.
     */
    public ICProject cProject() {
        IProject project = fFocusType.getCache().getProject();
        return findCProject(project);
    }
   	private ICProject findCProject(IProject project) {
		try {
			ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
			if (cProjects != null) {
				for (int i = 0; i < cProjects.length; ++i) {
					ICProject cProject = cProjects[i];
					if (project.equals(cProjects[i].getProject()))
						return cProject;
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getAllClasses()
     */
    public ICElement[] getAllClasses() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getRootClasses()
     */
    public ICElement[] getRootClasses() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getSubtypes(org.eclipse.cdt.core.model.ICElement)
     */
    public ICElement[] getSubtypes(ICElement type) {
	    List list = new ArrayList();
	    ITypeInfo info = TypeUtil.getTypeForElement(type);
		Collection entries = (Collection) fTypeToSubTypes.get(info);
		if (entries != null) {
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				ITypeInfo subType = (ITypeInfo)i.next();
				ICElement elem = TypeUtil.getElementForType(subType);
				if (elem != null) {
				    list.add(elem);
				}
			}
		}
		return (ICElement[])list.toArray(new ICElement[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getAllSuperclasses(org.eclipse.cdt.core.model.ICElement)
     */
    public ICElement[] getAllSubtypes(ICElement type) {
	    List list = new ArrayList();
	    ITypeInfo info = TypeUtil.getTypeForElement(type);
	    addSubs(info, list);
	    //convert list to ICElements
	    ICElement[] elems = new ICElement[list.size()];
	    int count = 0;
	    for (Iterator i = list.iterator(); i.hasNext(); ) {
	        ITypeInfo subType = (ITypeInfo) i.next();
	        elems[count++] = TypeUtil.getElementForType(subType);
	    }
	    return elems;
    }

    private void addSubs(ITypeInfo type, List list) {
		Collection entries = (Collection) fTypeToSubTypes.get(type);
		if (entries != null) {
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				ITypeInfo subType = (ITypeInfo)i.next();
				if (!list.contains(subType)) {
				    list.add(subType);
				}
			    addSubs(subType, list);
			}
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getSupertypes(org.eclipse.cdt.core.model.ICElement)
     */
    public ICElement[] getSupertypes(ICElement type) {
	    List list = new ArrayList();
	    ITypeInfo info = TypeUtil.getTypeForElement(type);
		Collection entries = (Collection) fTypeToSuperTypes.get(info);
		if (entries != null) {
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				TypeEntry entry = (TypeEntry)i.next();
				ITypeInfo superType = entry.type;
				ICElement elem = TypeUtil.getElementForType(superType);
				if (elem != null) {
				    list.add(elem);
				}
			}
		}
		return (ICElement[])list.toArray(new ICElement[list.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getAllSuperclasses(org.eclipse.cdt.core.model.ICElement)
     */
    public ICElement[] getAllSupertypes(ICElement type) {
	    List list = new ArrayList();
	    ITypeInfo info = TypeUtil.getTypeForElement(type);
	    addSupers(info, list);
	    //convert list to ICElements
	    ICElement[] elems = new ICElement[list.size()];
	    int count = 0;
	    for (Iterator i = list.iterator(); i.hasNext(); ) {
	        ITypeInfo superType = (ITypeInfo) i.next();
	        elems[count++] = TypeUtil.getElementForType(superType);
	    }
	    return elems;
    }

    private void addSupers(ITypeInfo type, List list) {
		Collection entries = (Collection) fTypeToSuperTypes.get(type);
		if (entries != null) {
			for (Iterator i = entries.iterator(); i.hasNext(); ) {
				TypeEntry entry = (TypeEntry)i.next();
				ITypeInfo superType = entry.type;
				if (!list.contains(superType)) {
				    list.add(superType);
				}
			    addSupers(superType, list);
			}
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#getType()
     */
    public ICElement getType() {
        if (fFocusType != null)
            return TypeUtil.getElementForType(fFocusType);
        return null;
    }

    /**
     * @see ITypeHierarchy
     * TODO (jerome) should use a PerThreadObject to build the hierarchy instead of synchronizing
     * (see also isAffected(IJavaElementDelta))
     */
    public synchronized void refresh(IProgressMonitor monitor) throws CModelException {
    	try {
    		fProgressMonitor = monitor;
    		if (monitor != null) {
    			if (fFocusType != null) {
    				monitor.beginTask(TypeHierarchyMessages.getFormattedString("hierarchy.creatingOnType", fFocusType.getQualifiedTypeName().getFullyQualifiedName()), 100); //$NON-NLS-1$
    			} else {
    				monitor.beginTask(TypeHierarchyMessages.getString("hierarchy.creating"), 100); //$NON-NLS-1$
    			}
    		}
    		long start = -1;
    		if (DEBUG) {
    			start = System.currentTimeMillis();
    			if (fComputeSubtypes) {
    				System.out.println("CREATING TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    			} else {
    				System.out.println("CREATING SUPER TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    			}
    			if (fFocusType != null) {
    				System.out.println("  on type " + fFocusType.toString()); //$NON-NLS-1$
    			}
    		}

    		compute();
//    		initializeRegions();
    		fNeedsRefresh = false;
//    		fChangeCollector = null;

    		if (DEBUG) {
    			if (fComputeSubtypes) {
    				System.out.println("CREATED TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
    			} else {
    				System.out.println("CREATED SUPER TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
    			}
    			System.out.println(this.toString());
    		}
    	} catch (CModelException e) {
    		throw e;
    	} catch (CoreException e) {
    		throw new CModelException(e);
    	} finally {
    		if (monitor != null) {
    			monitor.done();
    		}
    		fProgressMonitor = null;
    	}
    }

    /**
     * Compute this type hierarchy.
     */
    protected void compute() throws CModelException, CoreException {
    	if (fFocusType != null) {
//    		HierarchyBuilder builder = 
//    			new IndexBasedHierarchyBuilder(
//    				this, 
//    				this.scope);
//    		builder.build(this.computeSubtypes);
    	    
//			initialize(1);
//			buildSupertypes();
    	    
    	} // else a RegionBasedTypeHierarchy should be used
    }
    
    /**
     * Initializes this hierarchy's internal tables with the given size.
     */
 /*   protected void initialize(int size) {
    	if (size < 10) {
    		size = 10;
    	}
    	int smallSize = (size / 2);
    	this.classToSuperclass = new HashMap(size);
    	this.interfaces = new ArrayList(smallSize);
    	this.missingTypes = new ArrayList(smallSize);
    	this.rootClasses = new TypeVector();
    	this.typeToSubtypes = new HashMap(smallSize);
    	this.typeToSuperInterfaces = new HashMap(smallSize);
    	this.typeFlags = new HashMap(smallSize);
    	
    	this.projectRegion = new Region();
    	this.packageRegion = new Region();
    	this.files = new HashMap(5);
    }
*/
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy#store(java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void store(OutputStream outputStream, IProgressMonitor monitor) throws CModelException {
        // TODO Auto-generated method stub
        
    }

}
