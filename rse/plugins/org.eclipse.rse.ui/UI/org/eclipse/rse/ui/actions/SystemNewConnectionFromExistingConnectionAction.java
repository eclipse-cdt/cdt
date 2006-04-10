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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Create Another Connection wizard
 */
public class SystemNewConnectionFromExistingConnectionAction extends SystemNewConnectionAction 
                                 
{
	
	/**
	 * Constructor
	 * @param shell The parent shell to host the new wizard
	 */
	public SystemNewConnectionFromExistingConnectionAction(Shell shell)
	{
		super(shell, SystemResources.ACTION_ANOTHERCONN_LABEL, SystemResources.ACTION_ANOTHERCONN_TOOLTIP, false, true, null);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);
		setHelp(SystemPlugin.HELPPREFIX+"actn0015");
	}
}