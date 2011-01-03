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
package org.eclipse.rse.internal.useractions.api.ui.compile;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.UserActionsResources;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileCascadeByProfileAction;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.swt.widgets.Shell;

/**
 * Cascading Compile-> menu for remote compilable resources.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 */
public class SystemCascadingCompileAction extends SystemBaseSubMenuAction implements IMenuListener {
	private boolean isPrompt;

	/**
	 * Constructor for SystemCascadingCompileAction
	 */
	public SystemCascadingCompileAction(Shell shell, boolean isPrompt) {
		super(isPrompt ? UserActionsResources.ACTION_COMPILE_PROMPT_LABEL : UserActionsResources.ACTION_COMPILE_NOPROMPT_LABEL, isPrompt ? UserActionsResources.ACTION_COMPILE_PROMPT_TOOLTIP
				: UserActionsResources.ACTION_COMPILE_NOPROMPT_TOOLTIP, (ImageDescriptor) null, shell);
		this.isPrompt = isPrompt;
		allowOnMultipleSelection(false);
		setMenuID(ISystemContextMenuConstants.MENU_COMPILE);
		setCreateMenuEachTime(false);
		setPopulateMenuEachTime(true);
		//setTest(true);
		if (isPrompt)
			setHelp(RSEUIPlugin.HELPPREFIX + "ccpa0000"); //$NON-NLS-1$
		else
			setHelp(RSEUIPlugin.HELPPREFIX + "ccna0000"); //$NON-NLS-1$
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager ourSubMenu) {
		ourSubMenu.addMenuListener(this);
		ourSubMenu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		ourSubMenu.add(new SystemBaseAction("dummy", null)); //$NON-NLS-1$
		return ourSubMenu;
	}

	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu) {
		//System.out.println("Inside menuAboutToShow for SystemCascadingCompileAction");
		Object firstSelection = getFirstSelection();
		if (firstSelection == null) {
			System.out.println("Hmm, selection is null! "); //$NON-NLS-1$
			ourSubMenu.add(new SystemBaseAction("Programming error. Selection is null! ", null)); //$NON-NLS-1$
			return;
		}
		// is cascading-by-profile preference turned on?
		if (SystemPreferencesManager.getCascadeUserActions()) {
			ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
			for (int idx = 0; idx < activeProfiles.length; idx++) {
				SystemBaseSubMenuAction profileAction = new SystemCompileCascadeByProfileAction(getShell(), firstSelection, activeProfiles[idx], isPrompt);
				ourSubMenu.add(profileAction.getSubMenu());
			}
		}
		// else concatenate all the compile commands from all the active profiles...
		else {
			ISystemProfile[] activeProfiles = RSECorePlugin.getTheSystemRegistry().getActiveSystemProfiles();
			for (int idx = 0; idx < activeProfiles.length; idx++)
				SystemCompileCascadeByProfileAction.populateMenuWithCompileActions(ourSubMenu, getShell(), activeProfiles[idx], firstSelection, isPrompt);
		}
		// add a separator before Work With Compile Commands... menu item
		ourSubMenu.add(new Separator());
		// add Work With Commands... action
		ourSubMenu.add(new SystemWorkWithCompileCommandsAction(getShell(), true));
	}
}
