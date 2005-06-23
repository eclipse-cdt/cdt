/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ExternalSearchEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class ReferencesSearchGroup extends ActionGroup {

	private FindRefsAction fFindRefsAction;
	private FindRefsInWorkingSetAction fFindRefsInWorkingSetAction;
	
	private CEditor fEditor;
	private IWorkbenchSite fSite;
	
	private ArrayList actions;
	
	public ReferencesSearchGroup(IWorkbenchSite site) {
		fFindRefsAction= new FindRefsAction(site);
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
		fFindRefsInWorkingSetAction = new FindRefsInWorkingSetAction(editor, null);
	}
	
	/* 
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		
		super.fillContextMenu(menu);
		
		if ((fEditor != null) && (fEditor instanceof ExternalSearchEditor))
			return;
		
		IMenuManager incomingMenu = menu;
		
		IMenuManager refsMenu = new MenuManager(CSearchMessages.getString("group.references"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		
		if (fEditor != null){
			menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, refsMenu);	
		}
		
		incomingMenu.add(refsMenu);
		incomingMenu = refsMenu;
		
		FindAction[] actions = getWorkingSetActions();
		incomingMenu.add(fFindRefsAction);
		incomingMenu.add(fFindRefsInWorkingSetAction);
		
		for (int i=0; i<actions.length; i++){
			incomingMenu.add(actions[i]);
		}
		
	}	
	
	/**
	 * @return
	 */
	private FindAction[] getWorkingSetActions() {
		ArrayList actions= new ArrayList(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);
		
		Iterator iter= CSearchUtil.getLRUWorkingSets().iterator();
		while (iter.hasNext()) {
			IWorkingSet[] workingSets= (IWorkingSet[])iter.next();
			FindAction action;
			if (fEditor != null)
				action= new WorkingSetFindAction(fEditor, new FindRefsInWorkingSetAction(fEditor, workingSets), CSearchUtil.toString(workingSets));
			else
				action= new WorkingSetFindAction(fSite, new FindRefsInWorkingSetAction(fSite, workingSets), CSearchUtil.toString(workingSets));
			
			actions.add(action);
		}
		
		return (FindAction[])actions.toArray(new FindAction[actions.size()]);
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
