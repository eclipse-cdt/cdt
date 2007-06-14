/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import org.eclipse.cdt.refactoring.actions.CRefactoringActionGroup;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;

/**
 * A clone of org.eclipse.ui.internal.navigator.resources.actions.RefactorActionProvider.
 */
public class CNavigatorRefactorActionProvider extends CommonActionProvider {

	private UndoRedoActionGroup undoRedoGroup;
	private CNavigatorRefactorActionGroup resourceRefactorGroup;
	private CRefactoringActionGroup cElementRefactorGroup;

	private ICommonActionExtensionSite site;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite actionSite) {
		site = actionSite;
		resourceRefactorGroup= new CNavigatorRefactorActionGroup(site.getViewSite().getShell(), (Tree)site.getStructuredViewer().getControl());
		IUndoContext workspaceContext= (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		ICommonViewerWorkbenchSite workbenchSite = null;
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite = (ICommonViewerWorkbenchSite) site.getViewSite();
		}
		if (workbenchSite != null) {
			undoRedoGroup = new UndoRedoActionGroup(workbenchSite.getSite(), workspaceContext, true);
			cElementRefactorGroup= new CRefactoringActionGroup(workbenchSite.getPart());
		}
}

	public void dispose() {
		undoRedoGroup.dispose();
		resourceRefactorGroup.dispose();
		cElementRefactorGroup.dispose();
	}

	public void fillActionBars(IActionBars actionBars) {
		undoRedoGroup.fillActionBars(actionBars);
		resourceRefactorGroup.fillActionBars(actionBars);
		cElementRefactorGroup.updateActionBars();
		cElementRefactorGroup.fillActionBars(actionBars);
	}

	public void fillContextMenu(IMenuManager menu) {
		undoRedoGroup.fillContextMenu(menu);
		resourceRefactorGroup.fillContextMenu(menu);
		cElementRefactorGroup.fillContextMenu(menu);
	}

	public void setContext(ActionContext context) {
		undoRedoGroup.setContext(context);
		resourceRefactorGroup.setContext(convertToResources(context));
		cElementRefactorGroup.setContext(context);
	}

	public void updateActionBars() {
		undoRedoGroup.updateActionBars();
		resourceRefactorGroup.updateActionBars();
		cElementRefactorGroup.updateActionBars();
	}

	/**
	 * @param context
	 * @return context with selection elements converted to IResources
	 */
	private ActionContext convertToResources(ActionContext context) {
		if (context != null) {
			// convert non-IResource to IResources
			ISelection selection = SelectionConverter.convertSelectionToResources(context.getSelection());
			return new ActionContext(selection);
		}
		return null;
	}

}
