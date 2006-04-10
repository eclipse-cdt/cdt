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
import org.eclipse.rse.references.ISystemBasePersistableReferencedObject;

/**
 * This class extends the support for a class that supports being managing by a transient 
 * in-memory reference to one which also supports the persistance of such references.
 * To do this, such a referencable class must be able to return a name that is 
 * so unique that it can be used after restoration from disk to resolve a pointer to this
 * specific object, in memory.
 */
public class SystemPersistableReferencedObjectHelper 
       extends SystemReferencedObjectHelper
       implements ISystemBasePersistableReferencedObject 
{
	private String referenceName;
		
	/**
	 * Constructor for SystemPersistableReferencedObjectHelper
	 * @param referenceName The unique name that can be stored to identify this object.
	 */
	protected SystemPersistableReferencedObjectHelper(String referenceName) 
	{
		super();
		setReferenceName(referenceName);
	}
	
	/**
	 * Return the unique reference name of this object, as set in the constructor
	 */
	public String getReferenceName()
	{
		return referenceName;
	}
	
	/**
	 * Set the unique reference name of this object. Overrides what was set in
	 * the constructor. Typically called on rename operation.
	 */
	public void setReferenceName(String name)
	{
		this.referenceName = name;
	}

}