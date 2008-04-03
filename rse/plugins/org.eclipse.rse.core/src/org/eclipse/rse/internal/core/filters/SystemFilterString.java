/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - Cleanup Javadoc.
 * David Dykstal (IBM) - [224671] [api] org.eclipse.rse.core API leaks non-API types
 ********************************************************************************/

package org.eclipse.rse.internal.core.filters;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.references.SystemReferencedObjectHelper;
import org.eclipse.rse.internal.core.RSECoreMessages;


/**
 * This represents a filter string within a filter. Filters can hold one or more filter strings.
 */
public class SystemFilterString extends RSEModelObject implements ISystemFilterString, IAdaptable
{
    private ISystemFilter parentFilter;
    protected SystemReferencedObjectHelper helper = null;
    
	/**
	 * The default value of the '{@link #getString() <em>String</em>}' attribute.
	 * @see #getString()
	 */
	protected static final String STRING_EDEFAULT = null;

	protected String string = STRING_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * @see #getType()
	 */
	protected static final String TYPE_EDEFAULT = null;

	protected String type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * @see #isDefault()
	 */
	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;

	/**
     * Constructor. Do not instantiate yourself!
     */
	protected SystemFilterString() 
	{
		super();
		helper = new SystemReferencedObjectHelper();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);	
	}   

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#setParentSystemFilter(org.eclipse.rse.core.filters.ISystemFilter)
	 */
	public void setParentSystemFilter(ISystemFilter filter)
	{
		this.parentFilter = filter;
	}
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#getParentSystemFilter()
     */
    public ISystemFilter getParentSystemFilter()
    {
    	return parentFilter;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#getProvider()
     */
    public ISystemFilterPoolManagerProvider getProvider()
    {
        if (parentFilter != null)
          return parentFilter.getProvider();
        else
          return null;	
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#getSystemFilterPoolManager()
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager()
    {
        if (parentFilter != null)
          return parentFilter.getSystemFilterPoolManager();
        else
          return null;
    } 
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#getType()
     */
	public String getType()
    {
    	String type = this.getTypeGen();
    	if (type == null)
    	  return ISystemFilterConstants.DEFAULT_TYPE;
    	else
    	  return type;
    }
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#clone(org.eclipse.rse.core.filters.ISystemFilterString)
     */
    public void clone(ISystemFilterString targetString)
    {
    	String ourString = getString();
    	if (ourString != null)
    	  targetString.setString(new String(ourString));
    	targetString.setType(getTypeGen());
    	targetString.setDefault(isDefault());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.filters.ISystemFilterString#isChangable()
     */
    public boolean isChangable()
    {
	    boolean enable = !getParentSystemFilter().isNonChangable() &&
	                      !getParentSystemFilter().isStringsNonChangable();
	    return enable;
    }

	// ----------------------------------
	// IRSEReferencedObject methods...
	// ----------------------------------

    /*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencedObject#addReference(org.eclipse.rse.core.references.IRSEBaseReferencingObject)
	 */
	public int addReference(IRSEBaseReferencingObject ref)
	{
		return helper.addReference(ref);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencedObject#removeReference(org.eclipse.rse.core.references.IRSEBaseReferencingObject)
	 */
	public int removeReference(IRSEBaseReferencingObject ref)
	{
		return helper.removeReference(ref);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencedObject#getReferenceCount()
	 */
	public int getReferenceCount()
	{
		return helper.getReferenceCount();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencedObject#removeAllReferences()
	 */
	public void removeAllReferences()
	{
		helper.removeAllReferences();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.references.IRSEBaseReferencedObject#getReferencingObjects()
	 */
	public IRSEBaseReferencingObject[] getReferencingObjects()
	{
		return helper.getReferencingObjects();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public String getName()
	{
		return getString();
	}
	
	public String getDescription()
	{
		return RSECoreMessages.RESID_MODELOBJECTS_FILTERSTRING_DESCRIPTION;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#getString()
	 */
	public String getString()
	{
		return string;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#setString(java.lang.String)
	 */
	public void setString(String newString)
	{
		string = newString;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#setType(java.lang.String)
	 */
	public void setType(String newType)
	{
		type = newType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#isDefault()
	 */
	public boolean isDefault()
	{
		return default_;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.filters.ISystemFilterString#setDefault(boolean)
	 */
	public void setDefault(boolean newDefault)
	{
		default_ = newDefault;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (string: "); //$NON-NLS-1$
		result.append(string);
		result.append(", type: "); //$NON-NLS-1$
		result.append(type);
		result.append(", default: "); //$NON-NLS-1$
		result.append(default_);
		result.append(')');
		return result.toString();
	}

	/**
	 * Allows tools to have typed filter strings
	 */
	public String getTypeGen()
	{
		return type;
	}
	
	public boolean commit() 
	{
		boolean result = getParentSystemFilter().commit();
		return result;
	}
	
	public IRSEPersistableContainer getPersistableParent() {
		return parentFilter;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}

}