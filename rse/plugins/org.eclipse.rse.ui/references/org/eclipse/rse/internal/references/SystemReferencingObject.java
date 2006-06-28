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
import org.eclipse.rse.internal.model.RSEModelObject;
import org.eclipse.rse.references.ISystemBaseReferencedObject;
import org.eclipse.rse.references.ISystemReferencingObject;
import org.eclipse.rse.ui.SystemResources;


/**
 * A class to encapsulate the operations required of an object which
 * is merely a reference to another object, something we call a shadow.
 * Such shadows are needed to support a GUI which displays the same
 * object in multiple places. To enable that, it is necessary not to
 * use the same physical object in each UI representation as the UI
 * will only know how to update/refresh the first one it finds.
 * <p>
 * These references are not persistent. Persistent references are managed
 * by the subclass SystemPersistableReferencingObject.
 */
/** 
 * @lastgen class SystemReferencingObjectImpl Impl implements SystemReferencingObject, EObject {}
 */
public abstract class SystemReferencingObject extends RSEModelObject implements ISystemReferencingObject 
{
    private SystemReferencingObjectHelper helper = null;
    protected boolean referenceBroken = false;
		
	/**
	 * Default constructor. Typically called by EMF factory method.
	 */
	protected SystemReferencingObject() 
	{
		super();
		helper = new SystemReferencingObjectHelper(this);		
	}
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#setReferencedObject(ISystemBaseReferencedObject)
	 */
	public void setReferencedObject(ISystemBaseReferencedObject obj)
	{
        helper.setReferencedObject(obj);
	}
	
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#getReferencedObject()
	 */
	public ISystemBaseReferencedObject getReferencedObject()
	{
        return helper.getReferencedObject();
 	}
	
	/**
	 * @see org.eclipse.rse.references.ISystemBaseReferencingObject#removeReference()
	 */
	public int removeReference()
	{
        return helper.removeReference();
	}	    

	/**
	 * Set to true if this reference is currently broken/unresolved
	 */
	public void setReferenceBroken(boolean broken)
	{
		referenceBroken = broken;
	}
	/**
	 * Return true if this reference is currently broken/unresolved
	 */
	public boolean isReferenceBroken()
	{
		return referenceBroken;
	}
	
	public String getDescription()
	{
		return SystemResources.RESID_MODELOBJECTS_REFERENCINGOBJECT_DESCRIPTION;
	}
	
	protected final SystemReferencingObjectHelper getHelper() {
		return helper;
	}
}