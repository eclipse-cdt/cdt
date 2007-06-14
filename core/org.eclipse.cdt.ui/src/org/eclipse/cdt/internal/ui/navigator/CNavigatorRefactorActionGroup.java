/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.views.navigator.ResourceSelectionUtil;

/**
 * This is the action group for refactor actions move and rename.
 * 
 * A clone of org.eclipse.ui.internal.navigator.resources.actions.RefactorActionGroup.
 */
public class CNavigatorRefactorActionGroup extends ActionGroup {

	private RenameResourceAction renameAction;

	private MoveResourceAction moveAction;

	private Shell shell;

	private Tree tree;

	/**
	 *  
	 * @param aShell
	 * @param aTree
	 */
	public CNavigatorRefactorActionGroup(Shell aShell, Tree aTree) {
		shell = aShell;
		tree = aTree;
		makeActions();
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection)getContext().getSelection();
		boolean anyResourceSelected = !selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection,
						IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		if (anyResourceSelected) {
			moveAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, moveAction);
			renameAction.selectionChanged(selection);
			menu.insertAfter(moveAction.getId(), renameAction);
		}
	}

	public void fillActionBars(IActionBars actionBars) {
		updateActionBars();

//		textActionHandler.updateActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(), moveAction);
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
	}

	protected void makeActions() {
		moveAction = new MoveResourceAction(shell);
		moveAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.MOVE);
		
		renameAction = new RenameResourceAction(shell, tree);
		renameAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.RENAME);
	}

	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		moveAction.selectionChanged(selection);
		renameAction.selectionChanged(selection);
	}

}
