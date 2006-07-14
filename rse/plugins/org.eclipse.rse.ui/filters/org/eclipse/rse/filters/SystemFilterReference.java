/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.filters;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.filters.SystemFilterContainerReferenceCommonMethods;
import org.eclipse.rse.internal.filters.SystemFilterStringReference;
import org.eclipse.rse.internal.references.SystemReferencingObject;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemContentsType;
import org.eclipse.rse.references.ISystemBaseReferencedObject;
import org.eclipse.rse.references.ISystemReferencingObject;



/**
 * Represents a shadow or reference to a system filter.
 * Such references are only transient, not savable to disk.
 * All major function is inherited.
 * <p>
 * SystemFilter references typically exist for only one reason:
 * <ol>
 *  <li>As a simple shadow to enable a unique object in a GUI tree. For example,
 *       if it is possible for the same filter to show up in different places in 
 *       the tree, then we must create shadows for each place it shows up.
 * </ol>
 */
/**
 * @lastgen class SystemFilterReferenceImpl extends SystemReferencingObjectImpl implements IAdaptable, SystemFilterReference, SystemReferencingObject {}
 */
public class SystemFilterReference extends SystemReferencingObject implements IAdaptable, ISystemFilterReference, ISystemReferencingObject
{
	private SystemFilterContainerReferenceCommonMethods containerHelper = null;
	private ISystemFilterContainerReference parent = null; 	
	private ISystemFilter referencedFilter = null;
	private ISystemFilterStringReference[] referencedFilterStrings = null;
	protected boolean persistent;
	protected boolean isStale;
//	protected Object[] cachedContents;
	protected ISubSystem _subSystem;
	
	protected HashMap cachedContents;
	
	public static final boolean PERSISTENT_YES = true;
	public static final boolean PERSISTENT_NO  = false;
	/**
	 * Constructor. Typically called by MOF.
	 */
	protected SystemFilterReference() 
	{
		super();
		containerHelper = new SystemFilterContainerReferenceCommonMethods(this);
		persistent = true;
		isStale = true;
		cachedContents = new HashMap();
	}
	/**
	 * Create a new instance of this class.
	 * @param parent The SystemFilterReference or SystemFilterPoolReference object that we are a child of.
	 * @param filter The master object to be referenced.
	 * @param persistent Whether we should formally register our reference with the target filter or not.
	 */
	public static ISystemFilterReference createSystemFilterReference(ISubSystem subSystem,
																	ISystemFilterContainerReference parent, 
	                                                                ISystemFilter filter,
	                                                                boolean persistent)
	{
		//SystemFilterReferenceImpl newRef = (SystemFilterReferenceImpl)SystemFilterImpl.initMOF().createSystemFilterReference();
		SystemFilterReference newRef = new SystemFilterReference(); // more efficient?
		newRef.persistent = persistent;
		newRef.setSubSystem(subSystem);
		newRef.setParent(parent);
		newRef.setReferencedFilter(filter);		
		filter.addReference(newRef);
		return newRef;
	}

	/**
	 * Gets the subsystem that contains this reference
	 * @return the subsystem
	 */
	public ISubSystem getSubSystem()
	{
		return _subSystem;
	}
	
	/**
	 * Sets the subsystem that contains this reference
	 * @param subSystem
	 */
	public void setSubSystem(ISubSystem subSystem)
	{
		_subSystem = subSystem;
	}
	
	/**
	 * Return the reference manager which is managing this filter reference
	 * framework object.
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager()
	{
		ISystemFilterPoolReference pool = getParentSystemFilterReferencePool();
		if (pool != null)
		  return pool.getFilterPoolReferenceManager();
		else
		  return null;
	}
	
	/**
	 * Return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter reference
	 * framework object
	 */
    public ISystemFilterPoolReferenceManagerProvider getProvider()
    {
    	ISystemFilterPoolReferenceManager mgr = getFilterPoolReferenceManager();
    	if (mgr != null)
    	{
    		ISystemFilterPoolReferenceManagerProvider provider = mgr.getProvider();
    		if (provider == null)
    		{
    			provider = getSubSystem();
    		}
    		return provider;
    	}
    	else
    	  return null;
    }

    
    /**
     * If this is a reference to a nested filter, the parent is the
     * reference to the nested filter's parent. Else, it is the 
     * reference to the parent filter pool
     */
    public void setParent(ISystemFilterContainerReference parent)
    {
    	this.parent = parent;
    }    
    /**
     * The parent will either by a SystemFilterPoolReference or
     *  a SystemFilterReference.
     */
    public ISystemFilterContainerReference getParent()
    {
    	return parent;
    }

	/**
	 * Return the filter to which we reference...
	 */
	public ISystemFilter getReferencedFilter()
	{
		return persistent ? (ISystemFilter)super.getReferencedObject() : referencedFilter;
	}
	/**
	 * Set the filter to which we reference...
	 */
	public void setReferencedFilter(ISystemFilter filter)
	{
		if (persistent)
		  super.setReferencedObject(filter);
		else
		  referencedFilter = filter;
	}

    /**
     * If this is a reference to a nested filter, the parent is the
     * reference to the nested filter's parent. Else, it is the 
     * reference to the parent filter pool
     */
    public ISystemFilterPoolReference getParentSystemFilterReferencePool()
    {
    	if (parent instanceof ISystemFilterPoolReference)
    	  return (ISystemFilterPoolReference)parent;
    	else
    	  return ((ISystemFilterReference)parent).getParentSystemFilterReferencePool();
    }
    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }   
		
	// -------------------------------------------------------------
	// Methods common with SystemFilterPoolReferenceImpl, and hence
	//  abstracted out into SystemFilterContainerReference...
	// -------------------------------------------------------------
	/**
	 * Return the object to which we hold a reference. This is either
	 * SystemFilter or SystemFilterPool. Since both implement 
	 * SystemFilterContainer, that is what we return.
	 * <p>
	 * Of course, this is a generic method, and in our case it is always
	 * true that we only hold a SystemFilter. Hence, this is the same
	 * as calling getReferenceFilter and casting the result.
	 */
	public ISystemFilterContainer getReferencedSystemFilterContainer()
	{
		return getReferencedFilter();
	}
	/**
	 * Build and return an array of SystemFilterReference objects.
	 * Each object is created new. There is one for each of the filters
	 * in the reference SystemFilter or SystemFilterPool.
	 * For performance reasons, we will cache this array and only 
	 * return a fresh one if something changes in the underlying 
	 * filter list.
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem)
	{
		return containerHelper.getSystemFilterReferences(subSystem);
	}	
    /**
     * Create a single filter refererence to a given filter. 
     * If there already is a reference to this filter, it is returned.
     * If not, a new reference is created and appended to the end of the existing filter reference array.
     * @see #getExistingSystemFilterReference(ISystemFilter)
     */
    public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter)
    {
    	//return containerHelper.generateFilterReference(filter);
    	return containerHelper.generateAndRecordFilterReference(subSystem, filter);
    }	
    /**
     * Return an existing reference to a given system filter. 
     * If no reference currently exists to this filter, returns null.
     * @see #getSystemFilterReference(ISystemFilter)
     */
    public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter)
    {
    	return containerHelper.getExistingSystemFilterReference(subSystem, filter);
    }	
	
    /**
     * Return true if the referenced pool or filter has filters.
     */
    public boolean hasFilters()
    {
    	return containerHelper.hasFilters();
    }    

    /**
     * Return count of the number of filters in the referenced pool or filter
     */
    public int getFilterCount()
    {
    	return containerHelper.getFilterCount();
    }

    /**
     * Return the name of the SystemFilter or SystemFilterPool that we reference.
     * For such objects this is what we show in the GUI.
     */	
	public String getName()
	{
		ISystemFilter filter = getReferencedFilter();
		if (filter != null)
		  return filter.getName();
		else
		  return "";
	}
	
	/**
	 * Override of Object method. Turn this filter in an outputable string
	 */
	public String toString()
	{
		return getName();
	}
	
    // -------------------------------------------------
    // Methods for returning filter string references...
    // -------------------------------------------------
    /**
     * Return the number of filter strings in the referenced filter
     */
    public int getSystemFilterStringCount()
    {
    	int count = 0;
    	ISystemFilter referencedFilter = getReferencedFilter();
    	if (referencedFilter != null)
    	  count = referencedFilter.getFilterStringCount();
    	return count;
    }
    /**
     * Get the filter strings contained by this filter. But get references to each,
     *  not the masters.
     */
    public ISystemFilterStringReference[] getSystemFilterStringReferences()
    {    	
        // These reference objects are built on the fly, each time, rather than
        //  maintaining a persisted list of such references. The reason
        //  is we do no at this time allow users to subset the master list
        //  of strings maintained by a filter. Hence, we always simply
        //  return a complete list. However, to save memory we try to only
        //  re-gen the list if something has changed.
    	java.util.List mofList = getReferencedFilter().getStrings();
    	boolean needToReGen = compareFilterStrings(mofList);    	
    	if (needToReGen)
    	  referencedFilterStrings = generateFilterStringReferences(mofList);
    	return referencedFilterStrings;
    }

    /**
     * Create a single filter string refererence to a given filter string
     */
    public ISystemFilterStringReference getSystemFilterStringReference(ISystemFilterString filterString)
    {
    	return new SystemFilterStringReference((ISystemFilterReference)this, filterString);  
    }	

    
    /**
     * To save memory, we try to only regenerate the referenced filter list
     * if something has changed.
     */
    private boolean compareFilterStrings(java.util.List newFilterStrings)
    {
    	boolean mustReGen = false;
    	if (newFilterStrings == null)
    	{
    	  if (referencedFilterStrings != null)
    	    return true;
    	  else
    	    return false;
    	}
    	else if (referencedFilterStrings == null)
    	{
          return true; // newFilterStrings != null && referencedFilterStrings == null        	 
    	}
    	// both old and new are non-null
        if (newFilterStrings.size() != referencedFilterStrings.length)
          return true;
        Iterator i = newFilterStrings.iterator();
        for (int idx=0; !mustReGen && (idx<referencedFilterStrings.length); idx++)
        {
           ISystemFilterString newFilterString = (ISystemFilterString)i.next();
           if (!(referencedFilterStrings[idx].getReferencedFilterString().equals(newFilterString)))
             mustReGen = true;
        }               	
    	return mustReGen;
    }
        
    /**
     * To save the memory of an intermediate array, we create the filter string references 
     * directly from the MOF model...
     */
    private ISystemFilterStringReference[] generateFilterStringReferences(java.util.List newFilterStrings)
    {
    	if (newFilterStrings == null)
    	  return null;
    	ISystemFilterStringReference[] refs = new ISystemFilterStringReference[newFilterStrings.size()];
        Iterator i = newFilterStrings.iterator();
        int idx = 0;
        while (i.hasNext())
        {
    	   refs[idx++] = getSystemFilterStringReference((ISystemFilterString)i.next());    		            
        }               	
    	return refs;
    }
    

	// -----------------------------------
	// ISystemReferencingObject methods...
	// -----------------------------------

	/**
	 * Set the object to which we reference. Override of inherited
	 */
	public void setReferencedObject(ISystemBaseReferencedObject obj)
	{
		setReferencedFilter((ISystemFilter)obj);
	}
	/**
	 * Get the object which we reference. Override of inherited
	 */
	public ISystemBaseReferencedObject getReferencedObject()
	{
		return getReferencedFilter();
	}
	/**
	 * Fastpath to getReferencedObject().removeReference(this).
	 * @return new reference count of master object
	 */
	public int removeReference()
	{
		int count = 0;
		if (persistent)
		  super.removeReference();
		setReferencedFilter(null);
		return count;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.ISystemContainer#hasContents(org.eclipse.rse.model.ISystemContentsType)
     */
    public boolean hasContents(ISystemContentsType contentsType)
    {
        if (cachedContents.containsKey(contentsType))
        {
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.ISystemContainer#getContents(org.eclipse.rse.model.ISystemContentsType)
     */
    public Object[] getContents(ISystemContentsType contentsType)
    {
        return (Object[])cachedContents.get(contentsType);
    }
   
    public void setContents(ISystemContentsType type, Object[] cachedContents)
    {
        this.cachedContents.put(type, cachedContents);
        
        isStale = false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.ISystemContainer#isStale()
     */
    public boolean isStale()
    {
	      return isStale;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.ISystemContainer#markStale(boolean)
     */
    public void markStale(boolean isStale)
    {
    	markStale(isStale, true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.ISystemContainer#markStale(boolean)
     */
    public void markStale(boolean isStale, boolean clearCache)
    {
        this.isStale = isStale;
        if (clearCache && isStale)
        {
	        Iterator iterator = cachedContents.values().iterator();
	        while (iterator.hasNext())
	        {
	        	Object[] arr = (Object[])iterator.next();
	        	for (int i = 0; i < arr.length; i++)
	        	{
	        		Object obj = arr[i];
	        		if (obj instanceof ISystemContainer)
	        		{
	        			((ISystemContainer)obj).markStale(true);
	        		}
	        	}
	        }
	        cachedContents.clear();
        }
    }
	public boolean commit() 
	{
		// FIXME
		return false;
	}
	
    
}