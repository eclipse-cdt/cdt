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

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class ReferencesSearchGroup extends ActionGroup {

	private FindRefsAction fFindRefsAction;
	private FindRefsInWorkingSetAction fFindRefsInWorkingSetAction;
	
	private CEditor fEditor;
	private IWorkbenchSite fSite;
	
	public ReferencesSearchGroup(IWorkbenchSite site) {
		fFindRefsAction= new FindRefsAction(site);
		fFindRefsInWorkingSetAction = new FindRefsInWorkingSetAction(site);
	}
	
	/**
	 * @param editor
	 */
	public ReferencesSearchGroup(CEditor editor) {
		fEditor = editor;
		
		fFindRefsAction= new FindRefsAction(editor);
		fFindRefsAction.setActionDefinitionId(ICEditorActionDefinitionIds.FIND_REFS);
		if (editor != null){
			editor.setAction(ICEditorActionDefinitionIds.FIND_REFS, fFindRefsAction);
		}
		fFindRefsInWorkingSetAction = new FindRefsInWorkingSetAction(editor);
	}
	
	/* 
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
		
		IMenuManager refsMenu = new MenuManager(CSearchMessages.getString("group.references"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		
		if (fEditor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, refsMenu);	
		}
		
		incomingMenu.add(refsMenu);
		incomingMenu = refsMenu;
		
		incomingMenu.add(fFindRefsAction);
		incomingMenu.add(fFindRefsInWorkingSetAction);
		
	}	
	
	/* 
	 * Overrides method declared in ActionGroup
	 */
	public void dispose() {
		fFindRefsAction= null;
		fFindRefsInWorkingSetAction= null;
		super.dispose();
	}
}
