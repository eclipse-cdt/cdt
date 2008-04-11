/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;

public class ReferencesSearchGroup extends ActionGroup {

	private FindRefsAction fFindRefsAction;
	private FindRefsProjectAction fFindRefsProjectAction;
	private FindRefsInWorkingSetAction fFindRefsInWorkingSetAction;
	
	private CEditor fEditor;
	private IWorkbenchSite fSite;
	
	public ReferencesSearchGroup(IWorkbenchSite site) {
		fFindRefsAction= new FindRefsAction(site);
		fFindRefsProjectAction = new FindRefsProjectAction(site);
		fFindRefsInWorkingSetAction = new FindRefsInWorkingSetAction(site, null);
		fSite=site;
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
		fFindRefsProjectAction = new FindRefsProjectAction(editor);
		fFindRefsInWorkingSetAction = new FindRefsInWorkingSetAction(editor, null);
	}
	
	/* 
	 * Method declared on ActionGroup.
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
		
		IMenuManager refsMenu = new MenuManager(CSearchMessages.group_references, IContextMenuConstants.GROUP_SEARCH); 
		
		if (fEditor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, refsMenu);	
		} else {
			incomingMenu.appendToGroup(IContextMenuConstants.GROUP_SEARCH, refsMenu);
		}
		
		incomingMenu = refsMenu;
		
		FindAction[] actions = getWorkingSetActions();
		incomingMenu.add(fFindRefsAction);
		incomingMenu.add(fFindRefsProjectAction);
		incomingMenu.add(fFindRefsInWorkingSetAction);
		
		for (FindAction action : actions) {
			incomingMenu.add(action);
		}
		
	}	
	
	private FindAction[] getWorkingSetActions() {
		ArrayList<FindAction> actions= new ArrayList<FindAction>(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);
		
		Iterator<IWorkingSet[]> iter= CSearchUtil.getLRUWorkingSets().iterator();
		while (iter.hasNext()) {
			IWorkingSet[] workingSets= iter.next();
			FindAction action;
			if (fEditor != null)
				action= new WorkingSetFindAction(fEditor, new FindRefsInWorkingSetAction(fEditor, workingSets), CSearchUtil.toString(workingSets));
			else
				action= new WorkingSetFindAction(fSite, new FindRefsInWorkingSetAction(fSite, workingSets), CSearchUtil.toString(workingSets));
			
			actions.add(action);
		}
		
		return actions.toArray(new FindAction[actions.size()]);
	}
	
	/* 
	 * Overrides method declared in ActionGroup
	 */
	@Override
	public void dispose() {
		fFindRefsAction= null;
		fFindRefsProjectAction=null;
		fFindRefsInWorkingSetAction= null;
		super.dispose();
	}
}
