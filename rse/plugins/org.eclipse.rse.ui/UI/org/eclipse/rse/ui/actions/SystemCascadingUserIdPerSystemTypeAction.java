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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemType;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;



/**
 * A cascading menu action for "Set Default UserId->"
 */
public class SystemCascadingUserIdPerSystemTypeAction 
       extends SystemBaseSubMenuAction 
       implements IMenuListener
{
	
	/**
	 * Constructor 
	 */
	public SystemCascadingUserIdPerSystemTypeAction(Shell shell)
	{
		super(SystemResources.ACTION_CASCADING_USERID_LABEL, SystemResources.ACTION_CASCADING_USERID_TOOLTIP, shell);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
        setSelectionSensitive(false);
                
		setHelp(SystemPlugin.HELPPREFIX+"actn0010");
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		menu.add(new SystemBaseAction("dummy",null));
		return menu;
	}
	
	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu)
	{
		SystemType[] types = SystemPlugin.getTheSystemTypes(SystemPlugin.INCLUDE_LOCAL_NO); // false => do not include local
		for (int idx=0; idx<types.length; idx++)
		   ourSubMenu.add(new SystemPreferenceUserIdPerSystemTypeAction(getShell(),types[idx]));		
	}
}