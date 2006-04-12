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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.swt.widgets.Shell;

/**
 * Required interface for use in making contributions view the
 * adapter menu extension extension point (org.eclipse.rse.core.dynamicPopupMenuActions).
 */
public interface ISystemDynamicPopupMenuExtension 
{
	/**
	 * Returns true if this menu extension supports the specified selection.
	 * @param selection the resources to contriubte menu items to
	 * @return true if the extension will be used for menu population
	 */
	public boolean supportsSelection(IStructuredSelection selection);
	
	/**
	 * Populates the menu with specialized actions.
	 * @param shell the shell
	 * @param menu the menu to contribute actions to
	 * @param menuGroup the defect menu group to add actions to
	 * @param selection the resources to contriubte menu items to
	 * 
	 */	
	public void populateMenu(Shell shell, SystemMenuManager menu, IStructuredSelection selection, String menuGroup);
}