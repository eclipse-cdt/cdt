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
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterConstants;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.internal.references.SystemReferencedObjectHelper;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;


/**
 * This represents a filter string within a filter. Filters can hold one or more filter strings.
 */
/** 
 * @lastgen class SystemFilterStringImpl Impl implements SystemFilterString, IAdaptable {}
 */
public class SystemFilterString extends RSEModelObject implements ISystemFilterString, IAdaptable
{
    private ISystemFilter parentFilter;
    protected SystemReferencedObjectHelper helper = null;
    
	/**
	 * The default value of the '{@link #getString() <em>String</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getString()
	 * @generated
	 * @ordered
	 */
	protected static final String STRING_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String string = STRING_EDEFAULT;
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String type = TYPE_EDEFAULT;
	/**
	 * The default value of the '{@link #isDefault() <em>Default</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDefault()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DEFAULT_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean default_ = DEFAULT_EDEFAULT;
/**
     * Constructor. Do not instantiate yourself! Let MOF do it!
     */
	protected SystemFilterString() 
	{
		super();
		helper = new SystemReferencedObjectHelper();
	}
	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);	
	}   

	/**
	 * Set the transient parent back-pointer. Called by framework at restore/create time.
	 */
	public void setParentSystemFilter(ISystemFilter filter)
	{
		this.parentFilter = filter;
	}
    /**
     * Get the parent filter that contains this filter string.
     */
    public ISystemFilter getParentSystemFilter()
    {
    	return parentFilter;
    }
    /**
     * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
     */
    public ISystemFilterPoolManagerProvider getProvider()
    {
        if (parentFilter != null)
          return parentFilter.getProvider();
        else
          return null;	
    }
    /**
     * Return the filter pool manager managing this collection of filter pools and their filters and their filter strings.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager()
    {
        if (parentFilter != null)
          return parentFilter.getSystemFilterPoolManager();
        else
          return null;
    }    
    /**
     * Returns the type attribute. Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
     */
	public String getType()
    {
    	String type = this.getTypeGen();
    	if (type == null)
    	  return ISystemFilterConstants.DEFAULT_TYPE;
    	else
    	  return type;
    }
    /**
     * Clones this filter string's attributes into the given filter string
     */
    public void clone(ISystemFilterString targetString)
    {
    	String ourString = getString();
    	if (ourString != null)
    	  targetString.setString(new String(ourString));
    	targetString.setType(getTypeGen());
    	targetString.setDefault(isDefault());
    }

    /**
     * Is this filter string changable? Depends on mof attributes of parent filter
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
	

	public String getName()
	{
		return getString();
	}
	
	public String getDescription()
	{
		return SystemResources.RESID_MODELOBJECTS_FILTERSTRING_DESCRIPTION;
	}
	
	public String getString()
	{
		return string;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setString(String newString)
	{
		string = newString;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setType(String newType)
	{
		type = newType;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Is this a vendor-supplied filter string versus a user-defined filter string
	 */
	public boolean isDefault()
	{
		return default_;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDefault(boolean newDefault)
	{
		default_ = newDefault;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toString()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (string: ");
		result.append(string);
		result.append(", type: ");
		result.append(type);
		result.append(", default: ");
		result.append(default_);
		result.append(')');
		return result.toString();
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 * Allows tools to have typed filter strings
	 */
	public String getTypeGen()
	{
		return type;
	}
	
	public boolean commit() 
	{
		return RSEUIPlugin.getThePersistenceManager().commit(getParentSystemFilter());
	}

}