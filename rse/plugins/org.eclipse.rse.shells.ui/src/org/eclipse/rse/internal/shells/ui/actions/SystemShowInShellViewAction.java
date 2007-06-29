/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [165680] "Show in Remote Shell View" does not work
 *******************************************************************************/

package org.eclipse.rse.internal.shells.ui.actions;

import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.internal.shells.ui.view.SystemCommandsUI;
import org.eclipse.rse.internal.shells.ui.view.SystemCommandsViewPart;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Shell;



public class SystemShowInShellViewAction extends SystemBaseShellAction
{
	/**
	 * Constructor.
	 * @param parent Shell of parent window, used as the parent for the dialog.
	 *               Can be null, but be sure to call setParent before the action is used (ie, run).
	 */
	public SystemShowInShellViewAction(Shell parent)
	{
		super(ShellResources.ACTION_SHOW_SHELL_LABEL,			
				ShellResources.ACTION_SHOW_SHELL_TOOLTIP,
			RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHOW_SHELL_ID),
			parent);
		allowOnMultipleSelection(false);
	}

	/**
	 * Called when this action is selected from the popup menu.
	 */
	public void run()
	{
		SystemCommandsViewPart viewPart = SystemCommandsUI.getInstance().activateCommandsView();
		for (int i = 0; i < _selected.size(); i++)
		{
		    IRemoteCommandShell cmdShell = (IRemoteCommandShell)_selected.get(i);
		    viewPart.updateOutput(cmdShell);
		    viewPart.showPageFor(cmdShell); // for defect 165680		    
		}
	}
}
