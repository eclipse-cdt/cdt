/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search.actions;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;


public class DeclarationsSearchGroup extends ActionGroup {
	
	private CEditor fEditor;
	
	private FindDeclarationsAction fFindDeclarationsAction;
	private FindDeclarationsInWorkingSetAction fFindDeclarationsInWorkingSetAction;
	
	public DeclarationsSearchGroup(IWorkbenchSite site) {
		fFindDeclarationsAction= new FindDeclarationsAction(site);
		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(site);
	}
	/**
	 * @param editor
	 */
	public DeclarationsSearchGroup(CEditor editor) {
		fEditor = editor;

		fFindDeclarationsAction= new FindDeclarationsAction(editor);
		fFindDeclarationsAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_DECL);
		if (editor != null){
			editor.setAction(ICEditorActionDefinitionIds.FIND_DECL, fFindDeclarationsAction);
		}
		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(editor);
	}
	/* 
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
	
		IMenuManager declarationsMenu = new MenuManager(CSearchMessages.getString("group.declarations"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		
		if (fEditor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, declarationsMenu);	
		}
		
		incomingMenu.add(declarationsMenu);
		incomingMenu = declarationsMenu;
		
		incomingMenu.add(fFindDeclarationsAction);
		incomingMenu.add(fFindDeclarationsInWorkingSetAction);
	}	
	
	public static boolean canActionBeAdded(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return (((ITextSelection)selection).getLength() > 0);
		} else {
			return getElement(selection) != null;
		}
	}
	
	private static ICElement getElement(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
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
	public void dispose() {
		fFindDeclarationsAction= null;
		fFindDeclarationsInWorkingSetAction= null;
		super.dispose();
	}
}
