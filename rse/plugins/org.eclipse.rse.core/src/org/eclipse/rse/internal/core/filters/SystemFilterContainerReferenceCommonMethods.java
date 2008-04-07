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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.SystemFilterReference;
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
 * This class offers an implementation of those common methods, and both
 *  SystemFilterReferenceImpl and SystemFilterPoolReferenceImpl use this via
 *  containment.
 */
public class SystemFilterContainerReferenceCommonMethods {
	
	private ISystemFilterContainerReference parentRef = null;
	private List filterReferences = new ArrayList(10);

	/**
	 * Constructor
	 * @param parentRef the object we are helping.
	 */
	public SystemFilterContainerReferenceCommonMethods(ISystemFilterContainerReference parentRef) {
		super();
		this.parentRef = parentRef;
	}

	/**
	 * Return true if the referenced pool or filter has filters.
	 */
	public boolean hasFilters() {
		return getFilterCount() > 0;
	}

	/**
	 * Return count of the number of filters in the referenced pool or filter
	 */
	public int getFilterCount() {
		List filters = getFiltersFromParent();
		int count = filters.size();
		return count;
	}

	/**
	 * Get the list of nested filters referenced by this ISystemFilterContainerReference.
	 */
	public List getSystemFilterReferences(ISubSystem subSystem) {
		generateSystemFilterReferences(subSystem);
		return filterReferences;
	}

	/**
	 * Refreshes the list of filter references.
	 * @param subSystem
	 */
	private void generateSystemFilterReferences(ISubSystem subSystem) {
		List filters = getFiltersFromParent();
		if (mustGenerate(filters)) {
			generateFilterReferences(subSystem, filters);
		}
	}

	/**
	 * Determine if the list of filter references is stale.
	 */
	private boolean mustGenerate(List filters) {
		boolean result = true;
		List referencedFilters = getReferencedFilters();
		if (filters.size() == referencedFilters.size()) {
			int n = filters.size();
			int i = 0;
			result = false;
			while (i < n && result == false) {
				ISystemFilter filter = (ISystemFilter) filters.get(i);
				ISystemFilter referencedFilter = (ISystemFilter) referencedFilters.get(i);
				if (filter != referencedFilter) {
					result = true;
				}
				i++;
			}
		}
		return result;
	}
	
	/**
	 * @return the list of currently referenced filters
	 */
	private List getReferencedFilters() {
		List result = new ArrayList(filterReferences.size());
		for (Iterator z = filterReferences.iterator(); z.hasNext();) {
			ISystemFilterReference reference = (ISystemFilterReference) z.next();
			result.add(reference.getReferencedFilter());
		}
		return result;
	}

	/**
	 * Generate references to the filters in the list.
	 * Attempt to preserve existing references where they are available.
	 */
	private void generateFilterReferences(ISubSystem subSystem, List filters) {
		List referencedFilters = getReferencedFilters();
		List newReferences = new ArrayList(filters.size());
		for (Iterator z = filters.iterator(); z.hasNext();) {
			ISystemFilter filter = (ISystemFilter) z.next();
			int i = referencedFilters.indexOf(filter);
			ISystemFilterReference reference = (i >= 0) ? (ISystemFilterReference) filterReferences.get(i) : generateFilterReference(subSystem, filter);
			newReferences.add(reference);
		}
		filterReferences = newReferences;
	}

	/**
	 * Create a single filter refererence
	 */
	private ISystemFilterReference generateFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		ISystemFilterReference result = SystemFilterReference.createSystemFilterReference(subSystem, parentRef, filter, SystemFilterReference.PERSISTENT_NO);
		return result;
	}

	/**
	 * Create a single filter refererence and append it to the end of the list.
	 * This will first check if there already is a reference to the given filter and if so
	 * will simply return it.
	 */
	public ISystemFilterReference generateAndRecordFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		generateSystemFilterReferences(subSystem);
		ISystemFilterReference reference = getExistingSystemFilterReference(subSystem, filter);
		if (reference == null) {
			reference = generateFilterReference(subSystem, filter);
			filterReferences.add(reference);
		}
		return reference;
	}

	/**
	 * Return an existing reference to a given system filter. 
	 * If no reference currently exists to this filter, returns null.
	 */
	public ISystemFilterReference getExistingSystemFilterReference(ISubSystem subSystem, ISystemFilter filter) {
		ISystemFilterReference result = null;
		for (Iterator z = filterReferences.iterator(); z.hasNext() && result == null;) {
			ISystemFilterReference reference = (ISystemFilterReference) z.next();
			if (reference.getReferencedFilter() == filter && reference.getSubSystem() == subSystem) {
				result = reference;
			}
		}
		return result;
	}

	/**
	 * @return a list of ISystemFilter objects.
	 */
	private List getFiltersFromParent() {
		ISystemFilterContainer parent = parentRef.getReferencedSystemFilterContainer();
		ISystemFilter[] filters = null;
		if (parent instanceof ISystemFilterPool) {
			filters = ((ISystemFilterPool) parent).getFilters();
		} else if (parent instanceof ISystemFilter) {
			filters = ((ISystemFilter) parent).getNestedFilters();
		} else {
			filters = new ISystemFilter[0];
		}
		List result = Arrays.asList(filters);
		return result;
	}

}
