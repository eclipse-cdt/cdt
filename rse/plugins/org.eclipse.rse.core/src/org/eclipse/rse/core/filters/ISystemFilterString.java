/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - Cleanup Javadoc.
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.core.references.IRSEBaseReferencedObject;

/**
 * A filter string is a pattern used by the server-side code to know what to return to
 *  the client. A filter contains one or more filter strings. Basically, its nothing more
 *  than a string, and its up to each consumer to know what to do with it. Generally,
 *  a filter string edit pane is designed to prompt the user for the contents of the 
 *  string in a domain-friendly way.
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterString extends IRSEBaseReferencedObject, IAdaptable, IRSEModelObject {
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
	 * Check if this filter string is changeable.
	 * Depends on attributes of parent filter.
	 */
	public boolean isChangable();

	/**
	 * @return The value of the String attribute
	 */
	String getString();

	/**
	 * @param value The new value of the String attribute
	 */
	void setString(String value);

	/**
     * Returns the type attribute.
     * Intercepted to return SystemFilterConstants.DEFAULT_TYPE if it is currently null
	 * Allows tools to have typed filter strings
	 * @return The value of the Type attribute
	 */
	String getType();

	/**
	 * @param value The new value of the Type attribute
	 */
	void setType(String value);

	/**
	 * Check if this is a vendor-supplied filter string versus a user-defined filter string.
	 * @return The value of the Default attribute
	 */
	boolean isDefault();

	/**
	 * @param value The new value of the Default attribute
	 */
	void setDefault(boolean value);

}
