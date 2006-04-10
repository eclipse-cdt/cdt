/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.open;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.ui.IWorkbenchWindow;


public class SystemQuickOpenUI {

	/**
	 * Constructor.
	 */
	public SystemQuickOpenUI() {
		super();
	}
	
	/**
	 * Opens the quick open dialog in the active workbench window.
	 * If <code>pageId</code> is specified and a corresponding page is found then it is brought to top.
	 * @param pageId the page to select or <code>null</code> if the best fitting page should be selected.
	 */
	public static void openSearchDialog(String pageId) {
		openSearchDialog(SystemBasePlugin.getActiveWorkbenchWindow(), pageId);
	}
	
   /**
	* Opens the quick open dialog.
	* If <code>pageId</code> is specified and a corresponding page is found then it is brought to top.
	* @param window the workbench window to open the dialog in.
	* @param pageId	the page to select or <code>null</code> if the best fitting page should be selected.
	*/
   public static void openSearchDialog(IWorkbenchWindow window, String pageId) {
	   new SystemOpenQuickOpenDialogAction(window, pageId).run();
   }
}