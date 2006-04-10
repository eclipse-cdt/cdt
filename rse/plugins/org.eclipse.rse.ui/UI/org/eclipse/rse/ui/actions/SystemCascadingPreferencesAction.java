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
import org.eclipse.jface.action.Separator;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;



/**
 * A cascading menu action for "Preferences->".
 * @see org.eclipse.rse.ui.actions.SystemShowPreferencesPageAction
 */
public class SystemCascadingPreferencesAction 
       extends SystemBaseSubMenuAction implements IMenuListener
{
	
	/**
	 * Constructor 
	 */
	public SystemCascadingPreferencesAction(Shell shell)
	{
		super(SystemResources.ACTION_CASCADING_PREFERENCES_LABEL, SystemResources.ACTION_CASCADING_PREFERENCES_TOOLTIP, shell);
		setMenuID(ISystemContextMenuConstants.MENU_PREFERENCES);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
        setSelectionSensitive(false);
                
		setHelp(SystemPlugin.HELPPREFIX+"actnpref");
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager ourSubMenu)
	{
		// WE DON'T WANT TO FIRE UP ALL PLUGINS THAT USE OUR EXTENSION POINT, 
		// AT THE TIEM WE ARE CREATING OUR VIEW! SO WE DEFER IT UNTIL THIS CASCADING
		// MENU IS FIRST EXPANDED...
		ourSubMenu.addMenuListener(this);
		ourSubMenu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		ourSubMenu.add(new SystemBaseAction("dummy",null));
		
		return ourSubMenu;
	}

	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu)
	{
		//System.out.println("In menuAboutToShow!");
		setBusyCursor(true);
		ourSubMenu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
		SystemShowPreferencesPageAction[] prefPageActions = SystemPlugin.getDefault().getShowPreferencePageActions();
		if (prefPageActions!=null)
		{
			for (int idx=0; idx<prefPageActions.length; idx++)
			{
				prefPageActions[idx].setShell(getShell());
				ourSubMenu.appendToGroup(ISystemContextMenuConstants.GROUP_ADDITIONS, prefPageActions[idx]);
			}
		}
		setBusyCursor(false);
	}	
}