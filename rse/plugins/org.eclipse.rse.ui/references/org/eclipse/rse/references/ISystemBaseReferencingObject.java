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

package org.eclipse.rse.references;
/**
 * Referencing objects are shadows of real objects. Typically, shadows are created
 * to enable a GUI which does not allow the same real object to appear multiple times.
 * In these cases, a unique shadow object is created for each unique instance of the
 * real object.
 * <p>
 * This interface captures the simple set of methods such a shadow must implement.
 */
public interface ISystemBaseReferencingObject
{
	/**
	 * Set the object to which we reference
	 */
	public void setReferencedObject(ISystemBaseReferencedObject obj);
	/**
	 * Get the object which we reference
	 */
	public ISystemBaseReferencedObject getReferencedObject();
	/**
	 * Fastpath to getReferencedObject().removeReference(this).
	 * @return new reference count of master object
	 */
	public int removeReference();

	/**
	 * Set to true if this reference is currently broken/unresolved
	 */
	public void setReferenceBroken(boolean broken);
	
	/**
	 * Return true if this reference is currently broken/unresolved
	 */
	public boolean isReferenceBroken();	
}