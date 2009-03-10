/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.references.IRSEPersistableReferencingObject;

/**
 * Interface implemented by references to filter pools. Filter pools are stored at the profile
 * level, while subsystems contain references to one or more pools. A pool can be referenced
 * by multiple connections. Pools don't go away until explicitly deleted by the user, regardless
 * of their reference count.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterPoolReference extends IRSEPersistableReferencingObject, ISystemFilterContainerReference, IRSEModelObject {
	/**
	 * @return the reference manager which is managing this filter pool reference
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager();

	/**
	 * @return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter pool reference.
	 */
	public ISystemFilterPoolReferenceManagerProvider getProvider();

	/**
	 * @return the simple name of the filter pool we reference. Not qualified by the manager name.
	 */
	public String getReferencedFilterPoolName();

	/**
	 * @return name of the filter pool manager containing the pool we reference.
	 */
	public String getReferencedFilterPoolManagerName();

	/**
	 * Reset the name of the filter pool we reference. 
	 * Called on filter pool rename operations.
	 * @param newName the new name of the filer pool
	 */
	public void resetReferencedFilterPoolName(String newName);

	/**
	 * Set the filter pool that we reference.
	 * This should also call addReference(this) on that pool.
	 * @param pool the pool to which this reference refers.
	 */
	public void setReferenceToFilterPool(ISystemFilterPool pool);

	/**
	 * @return referenced filter pool object. 
	 * This may be null if the reference is broken
	 * or is yet to be resolved.
	 */
	public ISystemFilterPool getReferencedFilterPool();

	/**
	 * @return the fully qualified name that includes the name of the filter pool manager
	 */
	public String getFullName();
}