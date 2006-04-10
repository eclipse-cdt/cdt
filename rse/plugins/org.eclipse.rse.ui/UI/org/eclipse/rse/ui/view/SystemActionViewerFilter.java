/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * This class is a viewer filter that tests attributes of thise 
 */
public class SystemActionViewerFilter extends ViewerFilter {
	
	/**
	 * Inner class representing a filter criterion.
	 */
	private class FilterCriterion {
		
		private String name;
		private String value;
		
		/**
		 * Constructor.
		 * @param name the name.
		 * @param value the value.
		 */
		private FilterCriterion(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		/**
		 * Returns the name.
		 * @return the name.
		 */
		private String getName() {
			return name;
		}
		
		/**
		 * Returns the value.
		 * @return the value.
		 */
		private String getValue() {
			return value;
		}
	}
	
	// list to hold filter criteria for each object type
	private HashMap map;

	/**
	 * Constructor.
	 */
	public SystemActionViewerFilter() {
		super();
		map = new HashMap();
	}
	
	/**
	 * Adds a filter criterion.
	 * @param objectTypes object types that the filter criterion applies to.
	 * @param name the name.
	 * @param value the value.
	 */
	public void addFilterCriterion(Class[] objectTypes, String name, String value) {
		FilterCriterion criterion = new FilterCriterion(name, value);
		
		// go through each object type
		for (int i = 0; i < objectTypes.length; i++) {
			Class type = objectTypes[i];
			
			List criteria = null;
		
			// we do not have object type, so add it
			if (!map.containsKey(type)) {
				criteria = new ArrayList();
			}
			// we already have object type, so get its list of criteria
			else {
				criteria = (List)(map.get(type));
			}
			
			// add criterion to list
			criteria.add(criterion);
			
			// put type and list of criteria in map
			map.put(type, criteria);
		}
	}
	
	/**
	 * Removes all criteria.
	 */
	public void removeAllCriterion() {
		map.clear();
	}
	
	/**
	 * Checks if the object is an instance of any of the types in our list, and returns the
	 * type for which the object is an instance.
	 * @param obj the object.
	 * @return the type for which the object is an instance, or <code>null</code> if no type was
	 * found.
	 */
	private Class isInstance(Object obj) {
		
		// get set of types
		Set keySet = map.keySet();
		
		// get the iterator
		Iterator iter = keySet.iterator();
		
		// go through iterator
		while (iter.hasNext()) {
			Class objType = (Class)(iter.next());
			
			// check if object is an instance of the object type
			if (objType.isInstance(obj)) {
				return objType;
			}
		}
		
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		// check if the object is an instance of one of the object types we want to filter
		Class objType = isInstance(element);
		
		// no object type found, so let it through
		if (objType == null) {
			return true;
		}
		
		ISystemViewElementAdapter adapter = null;
		
		// get adapter
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable)element;
			
			adapter = (ISystemViewElementAdapter)(adaptable.getAdapter(ISystemViewElementAdapter.class));
			
			// get list of criteria
			List criteria = (List)(map.get(objType));
			
			// get iterator
			Iterator iter = criteria.iterator();
			
			// go through list of criterion, make sure one of them matches
			while (iter.hasNext()) {
				FilterCriterion criterion = (FilterCriterion)(iter.next());
				
				boolean testResult = adapter.testAttribute(element, criterion.getName(), criterion.getValue());
				
				if (testResult) {
					return true;
				}
			}
			
			return false;
		}
		
		return true;
	}
}