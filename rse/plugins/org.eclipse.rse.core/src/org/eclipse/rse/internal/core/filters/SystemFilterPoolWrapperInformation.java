/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.internal.core.filters;

import java.util.Vector;

import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapper;
import org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation;

/**
 * The system filter wizard allows callers to pass a list of wrapper objects for
 * the user to select a filter pool. Effectively, this prompting for euphemisms
 * to filter pools. This requires an array of wrapper objects, and requires
 * replacement mri for the pool prompt and tooltip text, and the verbiage above
 * it.
 * <p>
 * This is all encapsulated in this class. The information is set via setters or
 * constructor parameters.
 */
public class SystemFilterPoolWrapperInformation
	implements ISystemFilterPoolWrapperInformation
{
	private String promptLabel, promptTooltip, verbiageLabel;
	private Vector wrappers;
	private ISystemFilterPoolWrapper[] wrapperArray;
	private ISystemFilterPoolWrapper   preSelectWrapper;

	/**
	 * Constructor for SystemFilterPoolWrapperInformation.
	 */
	public SystemFilterPoolWrapperInformation(String promptLabel, String promptTooltip, String verbiageLabel)
	{
		super();
		this.promptLabel= promptLabel;
		this.verbiageLabel = verbiageLabel;
		this.promptLabel= promptTooltip;
		wrappers = new Vector();
	}

	/**
	 * Add a wrapper object
	 */
	public void addWrapper(ISystemFilterPoolWrapper wrapper)
	{
		wrappers.add(wrapper);
	}
	/**
	 * Add a filter pool, which we will wrapper here by creating a SystemFilterPoolWrapper object for you
	 */
	public void addWrapper(String displayName, ISystemFilterPool poolToWrap, boolean preSelect)
	{
		SystemFilterPoolWrapper wrapper = new SystemFilterPoolWrapper(displayName, poolToWrap);
		wrappers.add(wrapper);
		if (preSelect)
		  preSelectWrapper = wrapper;
	}
	/**
	 * Set the wrapper to preselect
	 */
	public void setPreSelectWrapper(ISystemFilterPoolWrapper wrapper)
	{
		this.preSelectWrapper = wrapper;
	}



	public String getPromptLabel()
	{
		return promptLabel;
	}

	public String getPromptTooltip()
	{
		return promptTooltip;
	}

	public String getVerbiageLabel()
	{
		return verbiageLabel;
	}


	/**
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation#getWrappers()
	 */
	public ISystemFilterPoolWrapper[] getWrappers()
	{
		if (wrapperArray == null)
		{
			wrapperArray = new ISystemFilterPoolWrapper[wrappers.size()];
			for (int idx=0; idx<wrapperArray.length; idx++)
			  wrapperArray[idx] = (ISystemFilterPoolWrapper)wrappers.elementAt(idx);
		}
		return wrapperArray;
	}

	/**
	 * @see org.eclipse.rse.core.filters.ISystemFilterPoolWrapperInformation#getPreSelectWrapper()
	 */
	public ISystemFilterPoolWrapper getPreSelectWrapper()
	{
		if (preSelectWrapper == null)
		{
			if (wrappers.size() > 0)
			  return (ISystemFilterPoolWrapper)wrappers.elementAt(0);
			else
			  return null;
		}
		else
		  return preSelectWrapper;
	}

}
