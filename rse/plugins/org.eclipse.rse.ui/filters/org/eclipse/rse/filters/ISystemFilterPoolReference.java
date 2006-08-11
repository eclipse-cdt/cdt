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
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.references.ISystemPersistableReferencingObject;


/**
 * Interface implemented by references to filter pools. Filter pools are stored at the profile
 *  level, while subsystems contain references to one or more pools. A pool can be referenced
 *  by multiple connections. Pools don't go away until explicitly deleted by the user, regardless
 *  of their reference count.
 */
/** 
 * @lastgen interface SystemFilterPoolReference extends SystemPersistableReferencingObject, ISystemPersistableReferencingObject, SystemFilterContainerReference  {}
 */
public interface ISystemFilterPoolReference extends ISystemPersistableReferencingObject, ISystemFilterContainerReference, IRSEModelObject
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
     * Return name of the filter pool we reference
     * The pool name is stored qualified by the manager name,
     *  so we first have to strip it off.
     */
    public String getReferencedFilterPoolName();
    /**
     * Return name of the filter pool manager containing the pool we reference.
     * The pool name is stored qualified by the manager name,
     *  so we get it from there.
     */
    public String getReferencedFilterPoolManagerName();

    /**
     * Reset the name of the filter pool we reference. 
     * Called on filter pool rename operations
     */
    public void resetReferencedFilterPoolName(String newName);

    
    /**
     * Set the filter pool that we reference.
     * This also calls addReference(this) on that pool!
     */
    public void setReferenceToFilterPool(ISystemFilterPool pool);
    
	/**
	 * Return referenced filter pool object
	 */
	public ISystemFilterPool getReferencedFilterPool();    
	
    /**
     * Return fully qualified name that includes the filter pool managers name
     */
    public String getFullName();	
}