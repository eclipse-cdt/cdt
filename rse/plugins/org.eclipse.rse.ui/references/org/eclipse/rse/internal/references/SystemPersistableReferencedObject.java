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
import org.eclipse.rse.references.ISystemBaseReferencingObject;
import org.eclipse.rse.references.ISystemPersistableReferencedObject;


/**
 * YOUR SUBCLASS MUST OVERRIDE getReferenceName()!
 * @see org.eclipse.rse.references.ISystemBasePersistableReferenceManager
 * @lastgen class SystemPersistableReferencedObjectImpl extends SystemReferencedObjectImpl implements SystemPersistableReferencedObject, SystemReferencedObject {}
 */
public abstract class SystemPersistableReferencedObject extends SystemReferencedObject implements ISystemPersistableReferencedObject {

	
    private SystemReferencedObjectHelper helper = null;	
	
	/**
	 * Constructor. Typically called by EMF framework via factory create method.
	 */
	public SystemPersistableReferencedObject() 
	{
		super();
		helper = new SystemReferencedObjectHelper();
	}
	/**
	 * Return the unique reference name of this object.
	 * <p>
	 * As required by the {@link org.eclipse.rse.references.ISystemBasePersistableReferencedObject ISystemPersistableReferencedObject} 
	 * interface.
	 * <p>
	 * YOUR SUBCLASS MUST OVERRIDE THIS!!
	 */
	public String getReferenceName()
	{
		return null;
	}
	
	// ----------------------------------
	// ISystemReferencedObject methods...
	// ----------------------------------
	/**
	 * Add a reference, increment reference count, return new count
	 * @return new count of how many referencing objects reference this object.
	 */
	public int addReference(ISystemBaseReferencingObject ref)
	{
		return helper.addReference(ref);
	}
	/**
	 * Remove a reference, decrement reference count, return new count
	 * @return new count of how many referencing objects reference this object.
	 */
	public int removeReference(ISystemBaseReferencingObject ref)
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
	public ISystemBaseReferencingObject[] getReferencingObjects()
	{
		return helper.getReferencingObjects();
	}
	
	
}