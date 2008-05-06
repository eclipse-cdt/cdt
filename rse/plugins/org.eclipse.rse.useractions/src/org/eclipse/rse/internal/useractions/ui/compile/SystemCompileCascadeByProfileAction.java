/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.ui.compile;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.view.SystemViewMenuListener;
import org.eclipse.rse.internal.useractions.api.files.compile.ISystemCompileManagerAdapter;
import org.eclipse.rse.internal.useractions.api.ui.compile.SystemCompileAction;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.ISystemViewMenuListener;
import org.eclipse.rse.ui.actions.SystemBaseDummyAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.widgets.Shell;

/**
 * A cascading submenu action for "Compile->".
 * This is after the first cascade, which lists profiles.
 * Here, for that profile, we list actions
 */
public class SystemCompileCascadeByProfileAction extends SystemBaseSubMenuAction implements IMenuListener {
	private ISystemProfile profile;
	private Object firstSelection;
	private boolean isPrompt;

	/**
	 * Constructor.
	 */
	public SystemCompileCascadeByProfileAction(Shell shell, Object firstSelection, ISystemProfile profile, boolean isPrompt) {
		super(profile.getName(), RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID), shell);
		this.profile = profile;
		this.firstSelection = firstSelection;
		this.isPrompt = isPrompt;
		setCreateMenuEachTime(false);
		setPopulateMenuEachTime(true);
		//this.setTest(true);
	}

	/**
	 * @see org.eclipse.rse.ui.actions.SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu) {
		//System.out.println("Inside populateSubMenu for SystemUDACascadeByProfileAction");
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		menu.add(new SystemBaseDummyAction());
		return menu;
	}

	/**
	 * Called when submenu is about to show. Called because we
	 * implement IMenuListener, and registered ourself for this event.
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu) {
		//System.out.println("Inside menuAboutToShow for SystemUDACascadeByProfileAction");
		Shell shell = getShell();
		populateMenuWithCompileActions(ourSubMenu, shell, profile, firstSelection, isPrompt);
	}

	/**
	 * Overridable method from parent that instantiates the menu listener who job is to add mnemonics.
	 * @param setMnemonicsOnlyOnce true if the menu is static and so mnemonics need only be set once. False if it is dynamic
	 */
	protected ISystemViewMenuListener createMnemonicsListener(boolean setMnemonicsOnlyOnce) {
		return new SystemViewMenuListener(false); // our menu is re-built dynamically each time
	}

	/**
	 * Re-usable method to populate a sub-menu with compile actions...
	 */
	public static IMenuManager populateMenuWithCompileActions(IMenuManager ourSubMenu, Shell shell, ISystemProfile profile, Object firstSelection, boolean isPrompt) {
		String srcType = null;
		ISystemRemoteElementAdapter rmtAdapter = SystemAdapterHelpers.getRemoteAdapter(firstSelection);
		if (rmtAdapter != null) {
			srcType = rmtAdapter.getRemoteSourceType(firstSelection);
			if (srcType == null)
				srcType = "null"; //$NON-NLS-1$
			else if (srcType.equals("")) //$NON-NLS-1$
				srcType = "blank"; //$NON-NLS-1$
		} else
			return ourSubMenu; // should never happen
		 ISubSystem subsystem = rmtAdapter.getSubSystem(firstSelection);
		 ISubSystemConfiguration ssc = subsystem.getSubSystemConfiguration();
		 
		 SystemCompileManager compileManager = null;
		 
		 if (firstSelection instanceof IAdaptable) {
			 ISystemCompileManagerAdapter	adapter = (ISystemCompileManagerAdapter)((IAdaptable)firstSelection).getAdapter(ISystemCompileManagerAdapter.class);
			 if (null != adapter)
			 {
				 compileManager = adapter.getSystemCompileManager(ssc);
			 }
		 }
		 
		 if (null != compileManager)
		 {
			 SystemCompileManager thisCompileManager = compileManager;
			 SystemCompileProfile compileProfile = thisCompileManager.getCompileProfile(profile);
			 // compileProfile.addContributions(firstSelection);
			 SystemCompileType compileType = compileProfile.getCompileType(srcType);
			 
			 if (compileType != null)
			 {
				 SystemCompileCommand[] cmds = compileType.getCompileCommandsArray();
				 for (int idx=0; idx<cmds.length; idx++)
				 {
					 SystemCompileAction action = new SystemCompileAction(shell, cmds[idx], isPrompt);
					 ourSubMenu.add(action);
				 }
			 }
		 }
		 
		return ourSubMenu;
	}
}
