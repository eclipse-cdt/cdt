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

package org.eclipse.rse.files.ui.resources;

/**
 * This class represents a set of markers.
 */
public class SystemRemoteMarkerSet {



	/**
	 *  constant indicating the minimum size
	 */
	private static final int MINIMUM_SIZE = 5;
	
	/**
	 * Number of elements in the set
	 */
	private int elementCount = 0;
	
	/**
	 * Array of elements in the set
	 */
	private ISystemRemoteMarkerSetElement[] elements;
	
	/**
	 * Constructor for SystemRemoteMarkerSet.
	 */
	public SystemRemoteMarkerSet() {
		this(MINIMUM_SIZE);
	}
	
	/**
	 * Constructor for SystemRemoteMarkerSet.
	 * @param the initial capacity
	 */
	public SystemRemoteMarkerSet(int capacity) {
		super();
		this.elements = new ISystemRemoteMarkerSetElement[Math.max(MINIMUM_SIZE, capacity * 2)];
	}
	
	/**
	 * Add an element to the set.
	 * @param the element to add to the set
	 */
	public void add(ISystemRemoteMarkerSetElement element) {
		
		if (element == null)
			return;
			
		int hash = hashFor(element.getId()) % elements.length;

		// search for an empty slot at the end of the array
		for (int i = hash; i < elements.length; i++) {
			
			if (elements[i] == null) {
				elements[i] = element;
				elementCount++;
				
				// grow if necessary
				if (shouldGrow()) {
					expand();
				}
				
				return;
			}
		}

		// search for an empty slot at the beginning of the array
		for (int i = 0; i < hash - 1; i++) {
			
			if (elements[i] == null) {
				elements[i] = element;
				elementCount++;
			
				// grow if necessary
				if (shouldGrow()) {
					expand();
				}
				
				return;
			}
		}

		// if we didn't find a free slot, then try again with the expanded set
		expand();
		add(element);
	}
	
	/**
	 * Add multiple elements.
	 * @param the elements to add to the set
	 */
	public void addAll(ISystemRemoteMarkerSetElement[] elements) {
		
		for (int i = 0; i < elements.length; i++) {
			add(elements[i]);
		}
	}
	
	/**
	 * Returns whether the set contains an element with the given id.
	 * @param the id to search for
	 * @return true if there is an element with the given id, false otherwise
	 */
	public boolean contains(long id) {
		return get(id) != null;
	}
	
	/**
	 * Get the elements in the set as an array.
	 * @return an array of elements that are in the set
	 */
	public ISystemRemoteMarkerSetElement[] elements() {
		ISystemRemoteMarkerSetElement[] result = new ISystemRemoteMarkerSetElement[elementCount];
		
		int j = 0;
		
		for (int i = 0; i < elements.length; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element != null) {
				result[j] = element;
				j++;
			}
		}
		
		return result;
	}
	
	/**
	 * Doubles the size of the internal array, and rehash all the values
	 */
	private void expand() {
		ISystemRemoteMarkerSetElement[] array = new ISystemRemoteMarkerSetElement[elements.length * 2];
		
		int maxArrayIndex = array.length - 1;
		
		for (int i = 0; i < elements.length; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element != null) {
				
				int hash = hashFor(element.getId()) % array.length;
				
				while (array[hash] != null) {
					
					hash++;
					
					if (hash > maxArrayIndex) {
						hash = 0;
					}
				}
				
				array[hash] = element;
			}
		}
		
		elements = array;
	}
	
	/**
	 * Returns the set element with the given id, or null if none
	 * is found.
	 * @param the id to search for
	 * @return the element, if found, or null
	 */
	public ISystemRemoteMarkerSetElement get(long id) {
		
		if (elementCount == 0) {
			return null;
		}
		
		int hash = hashFor(id) % elements.length;

		// search the last half of the array
		for (int i = hash; i < elements.length; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element == null) {
				return null;
			}
			
			if (element.getId() == id) {
				return element;
			}
		}

		// search the beginning of the array
		for (int i = 0; i < hash - 1; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element == null) {
				return null;
			}
			
			if (element.getId() == id) {
				return element;
			}
		}

		// no element found, so return null
		return null;
	}
	
	/**
	 * Hash key for the id.
	 * @param the id
	 * @return the hash value
	 */
	private int hashFor(long id) {
		return Math.abs((int) id);
	}
	
	/**
	 * Returns if the set is empty.
	 * @return true if the set is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return elementCount == 0;
	}
	
	/**
	 * Does a rehash when the element from the given index is removed.
	 * @param the index of the element removed.
	 */
	private void rehashTo(int anIndex) {
		int target = anIndex;
		int index = anIndex + 1;

		if (index >= elements.length) {
			index = 0;
		}
		
		ISystemRemoteMarkerSetElement element = elements[index];
		
		while (element != null) {
			 
			int hashIndex = hashFor(element.getId()) % elements.length;
			boolean match;
			
			if (index < target) {
				match = !(hashIndex > target || hashIndex <= index);
			}
			else {
				match = !(hashIndex > target && hashIndex <= index);
			}
			if (match) {
				elements[target] = element;
				target = index;
			}
			
			index++;
			
			if (index >= elements.length) {
				index = 0;
			}
			
			element = elements[index];
		}
		
		elements[target] = null;
	}

	/**
	 * Removes an element with the given id from the set.
	 * @param the id of the element to remove.
	 */	
	public void remove(long id) {
		int hash = hashFor(id) % elements.length;

		for (int i = hash; i < elements.length; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element == null) {
				return;
			}
			if (element.getId() == id) {
				rehashTo(i);
				elementCount--;
			}
		}

		for (int i = 0; i < hash - 1; i++) {
			
			ISystemRemoteMarkerSetElement element = elements[i];
			
			if (element == null) {
				return;
			}
			if (element.getId() == id) {
				rehashTo(i);
				elementCount--;
			}
		}
	}
	
	/**
	 * Removes the given element from the set. Uses the element id
	 * to search for the element in the set.
	 * @param the element
	 */
	public void remove(ISystemRemoteMarkerSetElement element) {
		remove(element.getId());
	}
	
	/**
	 * Removes all of the elements in the given array from the set.
	 * @param the array of elements to remove
	 */
	public void removeAll(ISystemRemoteMarkerSetElement[] elements) {
		
		for (int i = 0; i < elements.length; i++) {
			remove(elements[i]);
		}
	}
	
	/**
	 * Returns whether the internal storage should grow. Currently, returns <code>true</coe>
	 * if internal array is more that 75% full.
	 * @return true if the internal storage should grow, false otherwise
	 */
	private boolean shouldGrow() {
		return elementCount > elements.length * 0.75;
	}
	
	/**
	 * Returns the number of elements in the set.
	 * @return the number of elements in the set.
	 */
	public int size() {
		return elementCount;
	}
}