/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;


public class DeclarationsSearchGroup extends ActionGroup {

	private CEditor fEditor;
	private IWorkbenchSite fSite;

	private FindDeclarationsAction fFindDeclarationsAction;
	private FindDeclarationsProjectAction fFindDeclarationsProjectAction;
	private FindDeclarationsInWorkingSetAction fFindDeclarationsInWorkingSetAction;

	public DeclarationsSearchGroup(IWorkbenchSite site) {
		fFindDeclarationsAction= new FindDeclarationsAction(site);
		fFindDeclarationsAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL);
		fFindDeclarationsProjectAction = new FindDeclarationsProjectAction(site);
		fFindDeclarationsProjectAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL_PROJECT);
		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(site,null);
		fFindDeclarationsInWorkingSetAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL_WORKING_SET);
		fSite = site;
	}
	/**
	 * @param editor
	 */
	public DeclarationsSearchGroup(CEditor editor) {
		fEditor = editor;

		fFindDeclarationsAction= new FindDeclarationsAction(editor);
		fFindDeclarationsAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL);
		editor.setAction(ICEditorActionDefinitionIds.FIND_DECL, fFindDeclarationsAction);

		fFindDeclarationsProjectAction = new FindDeclarationsProjectAction(editor);
		fFindDeclarationsProjectAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL_PROJECT);
		editor.setAction(ICEditorActionDefinitionIds.FIND_DECL_PROJECT, fFindDeclarationsProjectAction);
		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(editor,null);
		fFindDeclarationsInWorkingSetAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL_WORKING_SET);
		editor.setAction(ICEditorActionDefinitionIds.FIND_DECL_WORKING_SET, fFindDeclarationsInWorkingSetAction);
	}
	/*
	 * Method declared on ActionGroup.
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);

		IMenuManager incomingMenu = menu;

		IMenuManager declarationsMenu = new MenuManager(CSearchMessages.group_declarations, IContextMenuConstants.GROUP_SEARCH);

		if (fEditor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, declarationsMenu);
		} else {
			incomingMenu.appendToGroup(IContextMenuConstants.GROUP_SEARCH, declarationsMenu);
		}
		incomingMenu = declarationsMenu;

		FindAction[] actions = getWorkingSetActions();
		incomingMenu.add(fFindDeclarationsAction);
		incomingMenu.add(fFindDeclarationsProjectAction);
		incomingMenu.add(fFindDeclarationsInWorkingSetAction);

		for (FindAction action : actions) {
			incomingMenu.add(action);
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ICEditorActionDefinitionIds.FIND_DECL, fFindDeclarationsAction);
		actionBars.setGlobalActionHandler(ICEditorActionDefinitionIds.FIND_DECL_PROJECT, fFindDeclarationsProjectAction);
		actionBars.setGlobalActionHandler(ICEditorActionDefinitionIds.FIND_DECL_WORKING_SET, fFindDeclarationsInWorkingSetAction);
	}

	private FindAction[] getWorkingSetActions() {
		ArrayList<FindAction> actions= new ArrayList<FindAction>(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);

		Iterator<IWorkingSet[]> iter= CSearchUtil.getLRUWorkingSets().iterator();
		while (iter.hasNext()) {
			IWorkingSet[] workingSets= iter.next();
			FindAction action;
			if (fEditor != null)
				action= new WorkingSetFindAction(fEditor, new FindDeclarationsInWorkingSetAction(fEditor, workingSets), CSearchUtil.toString(workingSets));
			else
				action= new WorkingSetFindAction(fSite, new FindDeclarationsInWorkingSetAction(fSite, workingSets), CSearchUtil.toString(workingSets));

			actions.add(action);
		}

		return actions.toArray(new FindAction[actions.size()]);
	}
	public static boolean canActionBeAdded(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return (((ITextSelection)selection).getLength() > 0);
		}
		return getElement(selection) != null;
	}

	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List<?> list= ((IStructuredSelection)sel).toList();
			if (list.size() == 1) {
				Object element= list.get(0);
				if (element instanceof ICElement) {
					return (ICElement)element;
				}
			}
		}
		return null;
	}

	/*
	 * Overrides method declared in ActionGroup
	 */
	@Override
	public void dispose() {
		fFindDeclarationsAction= null;
		fFindDeclarationsProjectAction=null;
		fFindDeclarationsInWorkingSetAction= null;
		super.dispose();
	}
}
