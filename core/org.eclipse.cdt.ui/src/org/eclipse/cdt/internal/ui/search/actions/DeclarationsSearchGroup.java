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
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;


public class DeclarationsSearchGroup extends ActionGroup {
	
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
		fFindDeclarationsAction= new FindDeclarationsAction(editor);
		fFindDeclarationsInWorkingSetAction = new FindDeclarationsInWorkingSetAction(editor);
	}
	/* 
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		IMenuManager incomingMenu = menu;
		
		IMenuManager declarationsMenu = new MenuManager(CSearchMessages.getString("group.declarations"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		incomingMenu.add(declarationsMenu);
		incomingMenu = declarationsMenu;
		
		incomingMenu.add(fFindDeclarationsAction);
		incomingMenu.add(fFindDeclarationsInWorkingSetAction);
	}	
}
