/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *        IBM Corporation - initial API and implementation 
************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is the action group for all the resource navigator actions.
 * It delegates to several subgroups for most of the actions.
 * 
 * @see GotoActionGroup
 * @see OpenActionGroup
 * @see RefactorActionGroup
 * @see SortAndFilterActionGroup
 * @see WorkspaceActionGroup
 * 
 * @since 2.0
 */
public abstract class CViewActionGroup extends ActionGroup {

	/**
	 * The resource navigator.
	 */
	protected CView cview;
	
	/**
	 * Constructs a new navigator action group and creates its actions.
	 * 
	 * @param navigator the resource navigator
	 */
	public CViewActionGroup(CView cview) {
		this.cview = cview;
		makeActions();
	}
	
	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			URL installURL = plugin.getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}	

	/**
	 * Returns the resource navigator.
	 */
	public CView getCView() {
		return cview;
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
	 * Does nothing by default.
 	 */
	public void handleKeyPressed(KeyEvent event) {
	}

	/**
	 * Makes the actions contained in this action group.
	 */
	protected abstract void makeActions();
	
	/**
	 * Runs the default action in the group.
	 * Does nothing by default.
	 * 
	 * @param selection the current selection
	 */
	public void runDefaultAction(IStructuredSelection selection) {
	}

}
