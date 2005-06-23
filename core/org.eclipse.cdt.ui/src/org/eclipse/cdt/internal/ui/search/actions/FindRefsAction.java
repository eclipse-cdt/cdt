/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.ui.IWorkbenchSite;


public class FindRefsAction extends FindAction {
	/**
	 * @param editor
	 */
	public FindRefsAction(CEditor editor) {
		this(editor,
		CSearchMessages.getString("CSearch.FindReferencesAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesAction.tooltip")); //$NON-NLS-1$
	}
	
	public FindRefsAction(IWorkbenchSite site){
		this(site,
		CSearchMessages.getString("CSearch.FindReferencesAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesAction.tooltip")); //$NON-NLS-1$
	} 
	/**
	 * @param editor
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public FindRefsAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	/**
	 * @param site
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public FindRefsAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		return CSearchMessages.getString("WorkspaceScope"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScope(org.eclipse.core.resources.IProject)
	 */
    protected ICSearchScope getScope() {
        return SearchEngine.createWorkspaceScope();
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		return ICSearchConstants.REFERENCES;
	}
}
