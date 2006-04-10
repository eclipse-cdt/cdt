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

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Both SystemFilter and SystemFilterPool contain filters, so the
 *  common methods for filters are abstracted out in SystemFilterContainer,
 *  which both classes implement.
 * Both SystemFilterReference and SystemFilterPoolReference hold references
 *  to SystemFilterContainer objects (either SystemFilter or SystemFilterPool).
 *  There are a couple of methods that are common to both classes, related to
 *  getting an array of references to the filters that are held by the referenced
 *  object.
 * This interface captures those common methods, and both 
 *  SystemFilterReferenceImpl and SystemFilterPoolReferenceImpl
 *  implement this interface and hence these methods.
 * @see org.eclipse.rse.internal.filters.SystemFilterContainerReferenceCommonMethods
 */
public interface ISystemFilterContainerReference
{	
	/**
	 * Return the object to which we hold a reference. This is either
	 * SystemFilter or SystemFilterPool. Since both implement 
	 * SystemFilterContainer, that is what we return.
	 */
	public ISystemFilterContainer getReferencedSystemFilterContainer();
	/**
	 * Build and return an array of SystemFilterReference objects.
	 * Each object is created new. There is one for each of the filters
	 * in the reference SystemFilter or SystemFilterPool.
	 * For performance reasons, we will cache this array and only 
	 * return a fresh one if something changes in the underlying 
	 * filter list.
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem);
    /**
     * Return an existing reference to a given system filter. 
     * If no reference currently exists to this filter, returns null.
     * @see #getSystemFilterReference(ISystemFilter)
     */
    public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter);
    /**
     * Create a single filter refererence to a given filter
     * If there already is a reference to this filter, it is returned.
     * If not, a new reference is created and appended to the end of the existing filter reference array.
     * @see #getExistingSystemFilterReference(ISystemFilter)
     */
    public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter);
		
    /**
     * Return the name of the SystemFilter or SystemFilterPool that we reference.
     * For such objects this is what we show in the GUI.
     */	
	public String getName();

    /**
     * Return true if the referenced pool or filter has filters.
     */
    public boolean hasFilters();
    
    /**
     * Return count of the number of filters in the referenced pool or filter
     */
    public int getFilterCount();
}