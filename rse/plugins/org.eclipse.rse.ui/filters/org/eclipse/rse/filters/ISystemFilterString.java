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

package org.eclipse.rse.filters;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.references.IRSEBaseReferencedObject;
import org.eclipse.rse.model.IRSEModelObject;


/**
 * A filter string is a pattern used by the server-side code to know what to return to
 *  the client. A filter contains one or more filter strings. Basically, its nothing more
 *  than a string, and its up to each consumer to know what to do with it. Generally,
 *  a filter string edit pane is designed to prompt the user for the contents of the 
 *  string in a domain-friendly way.
 * @see org.eclipse.rse.ui.filters.SystemFilterStringEditPane 
 * @see org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog and
 * @see org.eclipse.rse.ui.filters.actions.SystemChangeFilterAction
 * @see org.eclipse.rse.ui.filters.dialogs.SystemNewFilterWizard and
 * @see org.eclipse.rse.ui.filters.actions.SystemNewFilterAction
 */
public interface ISystemFilterString extends IRSEBaseReferencedObject, IAdaptable, IRSEModelObject
{
    /**
     * Return the caller which instantiated the filter pool manager overseeing this filter framework instance
     */
    public ISystemFilterPoolManagerProvider getProvider();
    /**
     * Return the filter pool manager managing this collection of filter pools and their filters and their filter strings.
     */
    public ISystemFilterPoolManager getSystemFilterPoolManager();    
	/**
	 * Set the transient parent back-pointer. Called by framework at restore/create time.
	 */
	public void setParentSystemFilter(ISystemFilter filter);
    /**
     * Get the parent filter that contains this filter string.
     */
    public ISystemFilter getParentSystemFilter();
    /**
     * Clones this filter string's attributes into the given filter string
     */
    public void clone(ISystemFilterString targetString);    
    /**
     * Is this filter string changable? Depends on mof attributes of parent filter
     */
    public boolean isChangable();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the String attribute
	 */
	String getString();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the String attribute
	 */
	void setString(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Type attribute
	 * Allows tools to have typed filter strings
	 */
	String getType();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Type attribute
	 */
	void setType(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Default attribute
	 * Is this a vendor-supplied filter string versus a user-defined filter string
	 */
	boolean isDefault();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Default attribute
	 */
	void setDefault(boolean value);

}