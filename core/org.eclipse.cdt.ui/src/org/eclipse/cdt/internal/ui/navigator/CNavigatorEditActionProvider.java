/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.ui.actions.GenerateActionGroup;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Common Navigator action provider for clipboard actions.
 */
public class CNavigatorEditActionProvider extends CommonActionProvider {

	private CNavigatorEditActionGroup fEditGroup;
	private GenerateActionGroup fGenerateGroup;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		super.init(anActionSite);

		fEditGroup = new CNavigatorEditActionGroup(anActionSite.getViewSite().getShell());

		ICommonViewerWorkbenchSite workbenchSite = null;
		if (anActionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite = (ICommonViewerWorkbenchSite) anActionSite.getViewSite();
		}
		if (workbenchSite != null) {
			if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
				IViewPart viewPart = (IViewPart) workbenchSite.getPart();

				fGenerateGroup = new GenerateActionGroup(viewPart);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		fEditGroup.dispose();
		if (fGenerateGroup != null) {
			fGenerateGroup.dispose();
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		fEditGroup.fillActionBars(actionBars);
		if (fGenerateGroup != null) {
			fGenerateGroup.fillActionBars(actionBars);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		fEditGroup.fillContextMenu(menu);
		if (fGenerateGroup != null) {
			fGenerateGroup.fillContextMenu(menu);
		}
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
		if (fGenerateGroup != null) {
			fGenerateGroup.setContext(context);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		fEditGroup.updateActionBars();
		if (fGenerateGroup != null) {
			fGenerateGroup.updateActionBars();
		}
	}
}
