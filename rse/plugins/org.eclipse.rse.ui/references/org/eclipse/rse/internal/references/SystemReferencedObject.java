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

package org.eclipse.rse.internal.references;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.references.IRSEReferencedObject;
import org.eclipse.rse.internal.model.RSEModelObject;


/**
 * A class to encapsulate the operations required of an object which
 * supports references to it by other objects (SystemReferencingObject).
 * This type of class needs to support maintaining an in-memory list of
 * all who reference it so that list can be following on delete and
 * rename operations.
 * <p>
 * These references are not persistent. Persistent references are managed
 * by the subclass SystemPersistableReferencedObject.
 */
/** 
 * @lastgen class SystemReferencedObjectImpl Impl implements SystemReferencedObject, EObject {}
 */
public abstract class SystemReferencedObject extends RSEModelObject implements IRSEReferencedObject
{
    protected SystemReferencedObjectHelper helper = null;
	
	/**
	 * Default constructor. Typically called by EMF factory method.
	 */
	protected SystemReferencedObject() 
	{
		super();
		helper = new SystemReferencedObjectHelper();
	}
	// ----------------------------------
	// IRSEReferencedObject methods...
	// ----------------------------------
	/**
	 * Add a reference, increment reference count, return new count
	 * @return new count of how many referencing objects reference this object.
	 */
	public int addReference(IRSEBaseReferencingObject ref)
	{
		return helper.addReference(ref);
	}
	/**
	 * Remove a reference, decrement reference count, return new count
	 * @return new count of how many referencing objects reference this object.
	 */
	public int removeReference(IRSEBaseReferencingObject ref)
	{
		return helper.removeReference(ref);
	}
	/**
	 * Return a count of how many referencing objects reference this object.
	 */
	public int getReferenceCount()
	{
		return helper.getReferenceCount();
	}
	/**
	 * Clear the list of referenced objects.
	 */
	public void removeAllReferences()
	{
		helper.removeAllReferences();		
	}
	/**
	 * Return a list of all referencing objects of this object
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects()
	{
		return helper.getReferencingObjects();
	}
	
	
}