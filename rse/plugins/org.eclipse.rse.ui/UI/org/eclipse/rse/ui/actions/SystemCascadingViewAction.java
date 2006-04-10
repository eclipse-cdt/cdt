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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.ui.SystemResources;



/**
 * A cascading menu action for "View->"
 */
public class SystemCascadingViewAction extends SystemBaseSubMenuAction 
{
	
	/**
	 * Constructor 
	 */
	public SystemCascadingViewAction()
	{
		super(SystemResources.ACTION_CASCADING_VIEW_LABEL, SystemResources.ACTION_CASCADING_VIEW_TOOLTIP, null);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(true);
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		// we don't populate it. SystemView populates it by calling each adapter and letting them populate it.
		return menu;
	}
}