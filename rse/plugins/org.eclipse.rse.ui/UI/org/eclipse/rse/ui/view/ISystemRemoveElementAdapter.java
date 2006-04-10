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

/**
 * This interface must be implemented by adapters who must remove elements from
 * their list of children (not necessarily immediate children).
 */
public interface ISystemRemoveElementAdapter {
	
	/**
	 * Remove all children from the element.
	 * @param element the element.
	 * @return <code>true</code> if the children have been removed, <code>false</code>
	 * otherwise.
	 */
	public boolean removeAllChildren(Object element);
	
	/**
	 * Remove a child from the element.
	 * @param element the element.
	 * @param child the child to remove. Does not have to be an immediate child
	 * of the element.
	 * @return <code>true</code> if the child has been removed, <code>false</code>
	 * otherwise.
	 */
	public boolean remove(Object element, Object child);
}