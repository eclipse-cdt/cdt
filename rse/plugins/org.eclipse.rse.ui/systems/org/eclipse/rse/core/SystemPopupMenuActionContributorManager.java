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

package org.eclipse.rse.core;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.ui.IWorkbenchPart;


/**
 * This class provides for the management of all popup menus provided by the 
 * org.eclipse.rse.core.popupMenus extension point.
 * To that end, we must 
 * <ul>
 *   <li>Process the additional filtering attributes we added to the <objectContribution> tag
 *   <li>Forgot all the code to do matching by object class type. 
 *   We can't do that because all remote objects 
 *   might be of the same type. Instead we replace that code with code 
 *   to do matching via those additional
 *   filter attributes we described.
 * </ul>
 * @see SystemPopupMenuActionContributor
 */
public class SystemPopupMenuActionContributorManager {

	private static final String T_OBJECT_CONTRIBUTION = "objectContribution"; //$NON-NLS-1$
	private static final String POPUP_MENU_EXTENSION_POINT_ID = "org.eclipse.rse.ui.popupMenus"; //$NON-NLS-1$
	private static SystemPopupMenuActionContributorManager singleton;
	private Vector contributors = new Vector();

	/**
	 * Returns the singleton instance of this manager.
	 */
	public static SystemPopupMenuActionContributorManager getManager() {
		if (singleton == null) {
			singleton = new SystemPopupMenuActionContributorManager();
		}
		return singleton;
	}

	/**
	 * Constructor for SystemPopupMenuActionContributorManager
	 */
	public SystemPopupMenuActionContributorManager() {
		super();
		loadContributors();
	}

	/**
	 * Reads the registry, constructs contributors from the "objectContribution"
	 * elements found, and registers them in the RSE popup menu registry.
	 */
	private void loadContributors() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] popupMenus = registry.getConfigurationElementsFor(POPUP_MENU_EXTENSION_POINT_ID);
		for (int i = 0; i < popupMenus.length; i++) {
			IConfigurationElement popupMenu = popupMenus[i];
			if (popupMenu.getName().equals(T_OBJECT_CONTRIBUTION))
			{
				SystemPopupMenuActionContributor contributor = new SystemPopupMenuActionContributor(popupMenu);
				contributors.add(contributor);
			}
			else
			{
				IConfigurationElement[] popupMenuChildren = popupMenu.getChildren();
				for (int j = 0; j < popupMenuChildren.length; j++) {
					IConfigurationElement popupMenuChild = popupMenuChildren[j];
					if (popupMenuChild.getName().equals(T_OBJECT_CONTRIBUTION)) 
					{
						SystemPopupMenuActionContributor contributor = new SystemPopupMenuActionContributor(popupMenuChild);
						contributors.add(contributor);
					} 
					else 
					{
						//TODO: add a warning message for this
						SystemBasePlugin.logWarning("Invalid Tag found: " + popupMenuChild.getName()); 
					}
				}
			}
		}
	}
	
	/**
	 * Contributes submenus and/or actions applicable to the selection in the
	 * provided viewer into the provided popup menu.
	 * It is called from the SystemView class when filling the context menu.
	 * TODO: use actionIdOverrides list
	 * @param part the IWorkbenchPart in which the selection lives and the menu will appear
	 * @param popupMenu the SystemMenuManager (menu) in which the menu items are to be placed
	 * @param selectionProvider the ISelectionProvider that will give us access to the selected items in the view
	 * @param actionIdOverrides the List of overrides for the actions (currently ignored)
	 * @return true if anything was added to the menu
	 */
	public boolean contributeObjectActions(IWorkbenchPart part, SystemMenuManager popupMenu, ISelectionProvider selectionProvider, List actionIdOverrides) {
		/* get the selection */
		ISelection selection = selectionProvider.getSelection();
		if ((selection == null) || !(selection instanceof IStructuredSelection)) return false;
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		/* Convert the selection to an array.  This is an optimization since they must each be scanned several times. */
		Object[] selections = structuredSelection.toArray();
		
		/* Finds those contributors that match every selection.  Those that match only some are discarded. */
		Vector matchingContributors = new Vector(10); // 10 is arbitrary but reasonable bound
		for (Iterator z = contributors.iterator(); z.hasNext();) {
			boolean matches = true;
			SystemPopupMenuActionContributor contributor = (SystemPopupMenuActionContributor) z.next();
			for (int i = 0; i < selections.length && matches; i++) {
				Object object = selections[i];
				if (!contributor.isApplicableTo(object)) {
					matches = false;
				}
			}
			if (matches) {
				matchingContributors.add(contributor);
			}
		}
		
		/* Process the menu contributions. */
		int actualContributions = 0;
		for (Iterator z = matchingContributors.iterator(); z.hasNext();) {
			SystemPopupMenuActionContributor contributor = (SystemPopupMenuActionContributor) z.next();
			boolean contributed = contributor.contributeObjectMenus(popupMenu, selectionProvider);
			if (contributed) actualContributions++;
		}
		
		/* Process the object action contributions. */
		for (Iterator z = matchingContributors.iterator(); z.hasNext();) {
			SystemPopupMenuActionContributor contributor = (SystemPopupMenuActionContributor) z.next();
			boolean contributed = contributor.contributeObjectActions(part, popupMenu, selectionProvider, actionIdOverrides);
			if (contributed) actualContributions++;
		}

		/* return true if there were any contributions made */
		return (actualContributions > 0);
	}

}