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
 * Martin Oberhuber (Wind River) - [225506] Move Move RSEUIPlugin#getShowPreferencePageActions() to internal
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.swt.widgets.Shell;



/**
 * A cascading menu action for "Preferences->".
 * @see org.eclipse.rse.internal.ui.actions.SystemShowPreferencesPageAction
 */
public class SystemCascadingPreferencesAction
       extends SystemBaseSubMenuAction implements IMenuListener
{
	private static SystemShowPreferencesPageAction[] showPrefPageActions = null;

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

		setHelp(RSEUIPlugin.HELPPREFIX+"actnpref"); //$NON-NLS-1$
	}

	/**
	 * Return an array of action objects to show for the "Preferences..."
	 * submenu of the RSE System View. For contributing a fastpath action to
	 * jump to your preferences page, from the local pulldown menu of the Remote
	 * Systems view. This may return null if no such actions are registered.
	 * 
	 * @deprecated will be moved to using command/hander extension point as per
	 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=186769
	 */
	public SystemShowPreferencesPageAction[] getShowPreferencePageActions() {
		if (showPrefPageActions == null) {
			// add our own preferences page action hardcoded
			SystemShowPreferencesPageAction action = new SystemShowPreferencesPageAction();
			action.setPreferencePageID("org.eclipse.rse.ui.preferences.RemoteSystemsPreferencePage"); //$NON-NLS-1$
			// action.setPreferencePageCategory(preferencePageCategory)
			// action.setImageDescriptor(id);
			action.setText(SystemResources.ACTION_SHOW_PREFERENCEPAGE_LABEL);
			action.setToolTipText(SystemResources.ACTION_SHOW_PREFERENCEPAGE_TOOLTIP);
			action.setHelp("org.eclipse.rse.ui.aprefrse"); //$NON-NLS-1$
			showPrefPageActions = new SystemShowPreferencesPageAction[1];
			showPrefPageActions[0] = action;
		}
		return showPrefPageActions;
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
		ourSubMenu.add(new SystemBaseAction("dummy",null)); //$NON-NLS-1$

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
		SystemShowPreferencesPageAction[] prefPageActions = getShowPreferencePageActions();
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
