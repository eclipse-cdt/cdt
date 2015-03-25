/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import org.eclipse.cdt.ui.refactoring.actions.CRefactoringActionGroup;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;

/**
 * A clone of org.eclipse.ui.internal.navigator.resources.actions.RefactorActionProvider.
 */
public class CNavigatorRefactorActionProvider extends CommonActionProvider {

	private UndoRedoActionGroup undoRedoGroup;
	private CNavigatorRefactorActionGroup resourceRefactorGroup;
	private CRefactoringActionGroup cElementRefactorGroup;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite actionSite) {
		super.init(actionSite);
		ICommonViewerWorkbenchSite workbenchSite= null;
		if (actionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite = (ICommonViewerWorkbenchSite) actionSite.getViewSite();
		}
		if (workbenchSite != null) {
			final IWorkbenchPartSite partSite= workbenchSite.getSite();
			resourceRefactorGroup= new CNavigatorRefactorActionGroup(partSite, (Tree)actionSite.getStructuredViewer().getControl());
			IUndoContext workspaceContext= ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
			undoRedoGroup = new UndoRedoActionGroup(partSite, workspaceContext, true);
			cElementRefactorGroup= new CRefactoringActionGroup(workbenchSite.getPart());
		}
	}

	@Override
	public void dispose() {
		undoRedoGroup.dispose();
		resourceRefactorGroup.dispose();
		cElementRefactorGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		undoRedoGroup.fillActionBars(actionBars);
		resourceRefactorGroup.fillActionBars(actionBars);
		cElementRefactorGroup.updateActionBars();
		cElementRefactorGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		undoRedoGroup.fillContextMenu(menu);
		resourceRefactorGroup.fillContextMenu(menu);
		cElementRefactorGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		undoRedoGroup.setContext(context);
		resourceRefactorGroup.setContext(convertToResources(context));
		cElementRefactorGroup.setContext(context);
	}

	@Override
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
