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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;

public class SelectionSearchGroup extends ActionGroup {
	
	private CEditor fEditor;
	
	private DeclarationsSearchGroup fDeclarationsSearchGroup;
	private ReferencesSearchGroup fRefSearchGroup;
	
	public SelectionSearchGroup(CEditor editor){
		//TODO: Assert editor not null
		fEditor= editor;
	
		fDeclarationsSearchGroup= new DeclarationsSearchGroup(fEditor);
		fRefSearchGroup = new ReferencesSearchGroup(fEditor);
	}
	/**
	 * @param page
	 */
	public SelectionSearchGroup(Page page) {
		this(page.getSite());
	}
	/**
	 * @param site
	 */
	public SelectionSearchGroup(IWorkbenchSite site) {
		fDeclarationsSearchGroup= new DeclarationsSearchGroup(site);
		fRefSearchGroup = new ReferencesSearchGroup(site);
	}
	/* 
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		
		fDeclarationsSearchGroup.fillContextMenu(menu);
		fRefSearchGroup.fillContextMenu(menu);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		if (fDeclarationsSearchGroup != null) {
			fDeclarationsSearchGroup.dispose();
			fDeclarationsSearchGroup= null;
		}
		
		if (fRefSearchGroup != null) {
			fRefSearchGroup.dispose();
			fRefSearchGroup= null;
		}
		
		fEditor= null;
		
		super.dispose();
	}
}
