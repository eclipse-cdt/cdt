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

import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Both ISystemFilter and ISystemFilterPool may contain filters, so the
 * common methods for filters are abstracted out in SystemFilterContainer,
 * which both classes implement.
 * Both ISystemFilterReference and ISystemFilterPoolReference hold references
 * to ISystemFilterContainer objects.
 * There are methods common to both classes, related to
 * getting an array of references to the filters that are held by the referenced
 * object.
 * This interface captures those common methods, and both 
 * SystemFilterReference and SystemFilterPoolReference
 * implement this interface and hence these methods.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterContainerReference {
	/**
	 * @return the filter container object which is referenced.
	 */
	public ISystemFilterContainer getReferencedSystemFilterContainer();

	/**
	 * Build and return an array of SystemFilterReference objects.
	 * Each object is created new. There is one for each of the filters
	 * in the reference SystemFilter or SystemFilterPool.
	 * For performance reasons, we will cache this array and only 
	 * return a fresh one if something changes in the underlying 
	 * filter list.
	 * @param subSystem the subsystem from which to get the filter references.
	 */
	public ISystemFilterReference[] getSystemFilterReferences(ISubSystem subSystem);

	/**
	 * Finds an existing filter in a particular subsystem.
	 * @param subSystem the subsystem in which to look for the filter reference.
	 * @param filter the filter for which to look.
	 * @return an existing reference to a given system filter. 
	 * If no reference currently exists to this filter, returns null.
	 * @see #getSystemFilterReference(ISubSystem, ISystemFilter)
	 */
	public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter);

	/**
	 * Find or create a single filter refererence to a given filter.
	 * If there already is a reference to this filter, in this subsystem it will be returned.
	 * If not, a new reference is created and added at the end of the list of filter references.
	 * @param subSystem the subsystem in which to find or create the filter.
	 * @param filter the filter to for which to create a reference.
	 * @see #getExistingSystemFilterReference(ISubSystem, ISystemFilter)
	 */
	public ISystemFilterReference getSystemFilterReference(ISubSystem subSystem, ISystemFilter filter);

	/**
	 * @return the name of the SystemFilter or SystemFilterPool that we reference.
	 */
	public String getName();

	/**
	 * @return true if this container has filters.
	 */
	public boolean hasFilters();

	/**
	 * @return the number of filters in the referenced container
	 */
	public int getFilterCount();
}
