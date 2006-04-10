/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Singleton class for managing adapter menu extensions.
 * View adapters that support this feature, should call populateMenu to allow for
 * extended menu contributions.
 */
public class SystemDynamicPopupMenuExtensionManager implements
		ISystemDynamicPopupMenuExtensionManager 
{
	private static SystemDynamicPopupMenuExtensionManager _instance = new SystemDynamicPopupMenuExtensionManager();
	
	private List _extensions;
	
	private SystemDynamicPopupMenuExtensionManager()
	{		
		_extensions= new ArrayList();
	}
	
	public static SystemDynamicPopupMenuExtensionManager getInstance()
	{
		return _instance;
	}
	
	public void registerMenuExtension(ISystemDynamicPopupMenuExtension ext)
	{
		_extensions.add(ext);
	}
	
	/**
	 * Actions are added to a contribution menu.
	 * @param shell the shell
	 * @param menu the menu to contribute to
	 * @param selection(s) are processed to determine the resource source file
	 * @param menuGroup the default menu group to add actions to
	 * @return the menu is populated with actions 
	 */
	public void populateMenu(Shell shell, IMenuManager menu,IStructuredSelection selection, String menuGroup)
	{
		for (int i = 0; i <_extensions.size(); i++)
		{
			ISystemDynamicPopupMenuExtension extension = (ISystemDynamicPopupMenuExtension)_extensions.get(i);
			if (extension.supportsSelection(selection))
			{
				extension.populateMenu(shell, menu,selection, menuGroup);
			}
		}
	}

}