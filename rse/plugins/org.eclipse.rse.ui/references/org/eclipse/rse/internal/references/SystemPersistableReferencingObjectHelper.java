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
import org.eclipse.rse.references.ISystemBaseReferencedObject;
import org.eclipse.rse.references.ISystemBaseReferencingObject;

/**
 * This class extends the support for managing a transient in-memory reference
 * to include support for storing a persistable name that uniquely identifies that
 * object.
 */
public class SystemPersistableReferencingObjectHelper 
       extends SystemReferencingObjectHelper
       //implements ISystemPersistableReferencingObject
{
    private String masterObjectName = null;
    /**
     * Default constructor. 
     */
    protected SystemPersistableReferencingObjectHelper(ISystemBaseReferencingObject caller)
    {
    	super(caller);
    }
    
    /**
     * Constructor that saves effort of calling setReferencedObject.
     */
    public SystemPersistableReferencingObjectHelper(ISystemBaseReferencingObject caller, ISystemBasePersistableReferencedObject obj)
    {
    	this(caller);
    	setReferencedObject(obj);
    }
        
	/**
	 * Set the object to which we reference. This overload takes an
	 * ISystemPersistableReferencedObject so we can query its name for
	 * storage purposes.
	 */
	public void setReferencedObject(ISystemBasePersistableReferencedObject obj)
	{
		super.setReferencedObject((ISystemBaseReferencedObject)obj);
		this.masterObjectName = obj.getReferenceName();
	}
	
	/**
	 * Return the name uniquely identifying the object we are referencing.
	 */
	public String getReferencedObjectName()
	{
		return masterObjectName;
	}
	
}