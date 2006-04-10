/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.actions;

import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * When we support Expand-To menu items to expand a remote item via subsetting criteria, 
 *  we should also support an Expand-To->All action. This is it.
 */
public class SystemViewExpandToAllAction extends SystemViewExpandToBaseAction 
{



	/**
	 * Constructor for SystemViewExpandToAllAction.
	 * @param rb
	 * @param prefix
	 * @param image
	 * @param parent
	 */
	public SystemViewExpandToAllAction(Shell parent) 
	{
		super(SystemResources.ACTION_EXPAND_ALL_LABEL, SystemResources.ACTION_EXPAND_ALL_TOOLTIP,null, parent);
	}

	/**
	 * @see org.eclipse.rse.ui.actions.SystemViewExpandToBaseAction#getFilterString(Object)
	 */
	protected String getFilterString(Object selectedObject) 
	{
		return null;
	}

}