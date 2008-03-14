/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 *******************************************************************************/

package org.eclipse.rse.internal.useractions.ui.uda;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.ui.uda.actions.SystemWorkWithUDAsAction;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.actions.SystemBaseDummyAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.swt.widgets.Shell;

/**
 * A cascading menu action for "User actions->"
 */
public class SystemUDACascadeAction extends SystemBaseSubMenuAction implements IMenuListener {
	private SystemUDActionSubsystem udsubsystem;
	//private IStructuredSelection  selection;
	private SystemWorkWithUDAsAction wwAction;

	/**
	 * Constructor for the "User Actions" menu item.
	 * Expansion will either list profiles, or actions, depending on preferences setting
	 */
	public SystemUDACascadeAction(SystemUDActionSubsystem udsubsystem, IStructuredSelection selection) {
		super(SystemUDAResources.ACTION_UDA_CASCADE_LABEL, SystemUDAResources.ACTION_UDA_CASCADE_TOOLTIP, null);
		this.udsubsystem = udsubsystem;
		super.setSelection(selection);
		setCreateMenuEachTime(false);
		setPopulateMenuEachTime(true);
	}

	/**
	 * This is called by the parent class, in its getSubMenu() method.
	 * That in turn is called when this menu is added to its parent menu.
	 */
	public IMenuManager populateSubMenu(IMenuManager menu) {
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		menu.add(new SystemBaseDummyAction());
		//((SystemSubMenuManager)menu).setTracing(true);
		return menu;
	}

	/**
	 * Called when submenu is about to show, by JFace.
	 * It is part of the IMenuListener interface, and we are called
	 * because we registered ourself as a listener in our populateSubMenu
	 * method.
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu) {
		//System.out.println("UDA submenu AboutToShow():");
		Shell shell = getShell();
		// is cascading-by-profile preference turned off?
		//System.out.println("Preference setting: " + SystemPreferencesGlobal.getGlobalSystemPreferences().getCascadeUserActions());
		if (!SystemPreferencesManager.getCascadeUserActions()) {
			udsubsystem.addUserActions(ourSubMenu, getSelection(), null, shell);
		}
		// is cascading-by-profile preference turned on?
		else {
			ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
			for (int idx = 0; idx < activeProfiles.length; idx++) {
				SystemBaseSubMenuAction profileAction = new SystemUDACascadeByProfileAction(shell, udsubsystem, getSelection(), activeProfiles[idx]);
				ourSubMenu.add(profileAction.getSubMenu());
			}
		}
		ourSubMenu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));
		if (wwAction == null) {
			wwAction = new SystemWorkWithUDAsAction(shell, udsubsystem.getSubsystem(), udsubsystem);
			wwAction.setText(SystemUDAResources.RESID_WORKWITH_UDAS_ACTION_LABEL);
			wwAction.setToolTipText(SystemUDAResources.RESID_WORKWITH_UDAS_ACTION_TOOLTIP);
			wwAction.allowOnMultipleSelection(true);
		}
		ourSubMenu.appendToGroup(ISystemContextMenuConstants.GROUP_WORKWITH, wwAction);
	}

	/**
	 * Override for debugging
	 */
	public void setInputs(Shell shell, Viewer v, ISelection selection) {
		super.setInputs(shell, v, selection);
		//System.out.println("Inside setInputs for SystemCascadeAction");
	}
}
