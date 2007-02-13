/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;

public class FindRefsInWorkingSetAction extends FindInWorkingSetAction {
	
	public FindRefsInWorkingSetAction(CEditor editor, IWorkingSet[] workingSets) {
		super(editor,
				CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip"), //$NON-NLS-1$
				workingSets);
	}
	
	public FindRefsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] workingSets){
		super (site,
				CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip"), //$NON-NLS-1$
				workingSets);
	}
	
	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}
}
