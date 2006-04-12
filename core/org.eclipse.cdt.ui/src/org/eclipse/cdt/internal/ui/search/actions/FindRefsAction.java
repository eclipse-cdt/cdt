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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.ui.IWorkbenchSite;

public class FindRefsAction extends FindAction {

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

	public FindRefsAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}

	public FindRefsAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	protected String getScopeDescription() {
		return CSearchMessages.getString("WorkspaceScope"); //$NON-NLS-1$
	}

	protected ICElement[] getScope() {
        return null;
    }

	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}
}
