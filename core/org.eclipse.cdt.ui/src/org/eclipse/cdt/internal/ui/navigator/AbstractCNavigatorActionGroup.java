/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - adaptations for Common Navigator
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionGroup;

/**
 * A basic abstract action group implementation similar to 
 * {@link org.eclipse.cdt.internal.ui.cview.CViewActionGroup}, avoiding
 * the explicit dependency on {@link org.eclipse.cdt.internal.ui.cview.CView} to
 * allow reuse in the Common Navigator.
 * 
 * @see org.eclipse.cdt.internal.ui.cview.CViewActionGroup
 */
public abstract class AbstractCNavigatorActionGroup extends ActionGroup {

	/**
	 * The view part this action group is associated with.
	 */
	private final IViewPart fViewPart;

	/**
	 * Create a new action group associated  with given view part.
	 * 
	 * @param viewPart
	 *            the view part this action group is associated with, may not be
	 *            <code>null</code>.
	 */
	public AbstractCNavigatorActionGroup(IViewPart viewPart) {
		Assert.isNotNull(viewPart);
		fViewPart = viewPart;
		makeActions();
	}

	/**
	 * Provide access to the view part this action group has been registered with.
	 * 
	 * @return the view part
	 */
	protected IViewPart getViewPart() {
		return fViewPart;
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "/icons/" + relativePath; //$NON-NLS-1$
		URL iconURL = FileLocator.find(CUIPlugin.getDefault().getBundle(), new Path(iconPath), null);
		// it's safe to pass null
		return ImageDescriptor.createFromURL(iconURL);
	}

	/**
	 * Makes the actions contained in this action group.
	 */
	protected abstract void makeActions();
	
	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public abstract void fillContextMenu(IMenuManager menu);

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public abstract void fillActionBars(IActionBars actionBars);

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	public abstract void updateActionBars();

}
