/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;

/**
 * Common Navigator action provider for clipboard actions.
 */
public class CNavigatorEditActionProvider extends CommonActionProvider {
	 
	private CNavigatorEditActionGroup fEditGroup;

	private ICommonActionExtensionSite fSite;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		fSite = anActionSite;
		fEditGroup = new CNavigatorEditActionGroup(fSite.getViewSite().getShell());
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() { 
		fEditGroup.dispose();
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) { 
		fEditGroup.fillActionBars(actionBars);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) { 
		fEditGroup.fillContextMenu(menu);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	@Override
	public void setContext(ActionContext context) { 
		if (context != null) {
			// convert non-IResource to IResources on the fly
			ISelection selection = SelectionConverter.convertSelectionToResources(context.getSelection());
			fEditGroup.setContext(new ActionContext(selection));
		} else {
			fEditGroup.setContext(context);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() { 
		fEditGroup.updateActionBars();
	}
}
