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
import org.eclipse.rse.core.references.IRSEReferencingObject;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemContentsType;


/**
 * Represents a shadow or reference to a system filter.
 * Such references are only transient, not savable to disk.
 */
/**
 * @lastgen interface SystemFilterReference extends SystemReferencingObject, SystemFilterContainerReference {}
 */
public interface ISystemFilterReference extends IRSEReferencingObject, ISystemFilterContainerReference, ISystemContainer
{
	/**
	 * Return the reference manager which is managing this filter reference
	 * framework object.
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager();
	
	/**
	 * Return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter reference
	 * framework object
	 */
    public ISystemFilterPoolReferenceManagerProvider getProvider();    
    
    /**
	 * Gets the subsystem that contains this reference
	 * @return the subsystem
	 */
	public ISubSystem getSubSystem();
	
	/**
	 * Sets the subsystem that contains this reference
	 * @param subSystem
	 */
	public void setSubSystem(ISubSystem subSystem);
    
	/**
	 * Return the filter to which we reference...
	 */
	public ISystemFilter getReferencedFilter();
	/**
	 * Set the filter to which we reference...
	 */
	public void setReferencedFilter(ISystemFilter filter);

    /**
     * Get the parent of this reference.
     * It will be either a SystemFilterPoolReference, or
     *  a SystemFilterReference(if nested).
     */
    public ISystemFilterContainerReference getParent();
    /**
     * Get parent or super parent filter pool reference.
     */
    public ISystemFilterPoolReference getParentSystemFilterReferencePool();  
    
    // -------------------------------------------------
    // Methods for returning filter string references...
    // -------------------------------------------------
    /**
     * Return the number of filter strings in the referenced filter
     */
    public int getSystemFilterStringCount();
    /**
     * Get the filter strings contained by this filter. But get references to each,
     *  not the masters.
     */
    public ISystemFilterStringReference[] getSystemFilterStringReferences();    
    /**
     * Create a single filter string refererence to a given filter string
     */
    public ISystemFilterStringReference getSystemFilterStringReference(ISystemFilterString filterString);
    
    /*
     * Sets the cached contents for this filter reference.  If the filter changes or is refreshed, these cached
     * items will be removed.
     */
    public void setContents(ISystemContentsType type, Object[] cachedContents);

}