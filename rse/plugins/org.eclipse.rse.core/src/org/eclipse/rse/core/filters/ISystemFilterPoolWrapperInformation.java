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
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.filters;

/**
 * The system filter wizard allows callers to pass a list of wrapper objects for
 * the user to select a filter pool. Effectively, this prompting for euphemisms
 * to filter pools. This requires an array of wrapper objects, and requires
 * replacement text for the pool prompt and tooltip text, and the verbiage above
 * it.
 * <p>
 * This is all encapsulated in this interface. There is also a class offered
 * that implements all this and is populated via setters.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterPoolWrapperInformation {

	/**
	 * Get the label
	 */
	public String getPromptLabel();

	/**
	 * Get the tooltip
	 */
	public String getPromptTooltip();

	public String getVerbiageLabel();

	/**
	 * Get the list of wrappered filter pool objects to show in the combo. The wrappering allows
	 *  each to be displayed with a different name in the list than just pool.getName()
	 */
	public ISystemFilterPoolWrapper[] getWrappers();

	/**
	 * Get the wrapper to preselect in the list.
	 */
	public ISystemFilterPoolWrapper getPreSelectWrapper();

	/**
	 * Add a wrapper object
	 * @since 3.0
	 */
	public void addWrapper(ISystemFilterPoolWrapper wrapper);
	/**
	 * Add a filter pool, which we will wrapper here by creating a SystemFilterPoolWrapper object for you
	 * @since 3.0
	 */
	public void addWrapper(String displayName, ISystemFilterPool poolToWrap, boolean preSelect);

}
