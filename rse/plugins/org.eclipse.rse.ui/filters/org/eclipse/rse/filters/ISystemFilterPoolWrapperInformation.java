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
/**
 * The system filter wizard allows callers to pass a list of wrapper objects
 *  for the user to select a filter pool. Effectively, this prompting for 
 *  euphamisms to filter pools. This requires an array of wrapper objects,
 *  and requires replacement mri for the pool prompt and tooltip text, and 
 *  the verbage above it. 
 * <p>
 * This is all encapsulated in this interface. There is also a class offered
 *  that implements all this and is populated via setters.
 */
public interface ISystemFilterPoolWrapperInformation 
{

	/**
	 * Get the label
	 */
	public String getPromptLabel();
	
	/**
	 * Get the tooltip
	 */
	public String getPromptTooltip();

	public String getVerbageLabel();	
	/**
	 * Get the list of wrappered filter pool objects to show in the combo. The wrappering allows
	 *  each to be displayed with a different name in the list than just pool.getName()
	 */
	public ISystemFilterPoolWrapper[] getWrappers();
	/**
	 * Get the wrapper to preselect in the list.
	 */
	public ISystemFilterPoolWrapper getPreSelectWrapper();
}