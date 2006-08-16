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

package org.eclipse.rse.internal.filters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.references.IRSEBaseReferencedObject;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.internal.references.SystemReferencingObjectHelper;

/**
 * A reference to a system filter string.
 */
public class SystemFilterStringReference
	implements ISystemFilterStringReference, IAdaptable
{
    protected ISystemFilterReference parent;
    protected ISystemFilter parentFilter;
    protected SystemReferencingObjectHelper helper = null;
    protected boolean referenceBroken = false;

	/**
	 * Constructor for SystemFilterStringReferenceImpl
	 * @param parentRef The parent filter reference for this filter string reference.
	 * @param referencedString The filter string we reference
	 */
	public SystemFilterStringReference(ISystemFilterReference parentRef, ISystemFilterString referencedString) 
	{
		super();
		parent = parentRef;
		helper = new SystemReferencingObjectHelper(this);		
		setReferencedObject(referencedString);
	}
	/**
	 * Constructor for SystemFilterStringReferenceImpl when starting with filter vs filter reference parent
	 * <p>
	 * we are not yet ready to make this available.
	 * @param parentFilter The parent filter for this filter string reference.
	 * @param referencedString The filter string we reference
	 */
	protected SystemFilterStringReference(ISystemFilter parentFilter, ISystemFilterString referencedString) 
	{
		super();
		this.parentFilter = parentFilter;
		helper = new SystemReferencingObjectHelper(this);		
		setReferencedObject(referencedString);
	}


	/**
	 * Return the reference manager which is managing this filter reference
	 * framework object.
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager()
	{
		if (parent != null)
		  return parent.getFilterPoolReferenceManager();
		return null;
	}
	
	/**
	 * Return the object which instantiated the pool reference manager object.
	 * Makes it easy to get back to the point of origin, given any filter reference
	 * framework object
	 */
    public ISystemFilterPoolReferenceManagerProvider getProvider()
    {
    	ISystemFilterPoolReferenceManager mgr = getFilterPoolReferenceManager();
    	if (mgr != null)
    	  return mgr.getProvider();
    	else
    	  return null;
    }

	/**
	 * @see ISystemFilterStringReference#getReferencedFilterString()
	 */
	public ISystemFilterString getReferencedFilterString() 
	{
		return (ISystemFilterString)getReferencedObject();
	}

    /**
     * Same as getReferencedFilterString().getString()
     */
    public String getString()
    {
    	return getReferencedFilterString().getString();
    }
    

	/**
	 * @see ISystemFilterStringReference#getParent()
	 */
	public ISystemFilterReference getParent() 
	{
		return parent;
	}
	/**
	 * @see ISystemFilterStringReference#getParentSystemFilter()
	 */
	public ISystemFilter getParentSystemFilter() 
	{
		if (parentFilter != null)		
		  return parentFilter;
		else if (parent != null)
		  return parent.getReferencedFilter();
		else
		  return null;
	}


    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 * By default this returns Platform.getAdapterManager().getAdapter(this, adapterType);
	 * This in turn results in the default subsystem adapter SystemViewSubSystemAdapter,
	 * in package org.eclipse.rse.ui.view. 
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }    
	// ----------------------------------------------
	// IRSEReferencingObject methods...
	// ----------------------------------------------
	
	/**
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#setReferencedObject(IRSEBaseReferencedObject)
	 */
	public void setReferencedObject(IRSEBaseReferencedObject obj)
	{
        helper.setReferencedObject(obj);
	}
	
	/**
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#getReferencedObject()
	 */
	public IRSEBaseReferencedObject getReferencedObject()
	{
        return helper.getReferencedObject();
 	}
	
	/**
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencingObject#removeReference()
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
}