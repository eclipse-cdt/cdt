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

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchScopeFactory;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindRefsInWorkingSetAction extends FindAction {
	
	public FindRefsInWorkingSetAction(CEditor editor) {
		this(editor,
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip")); //$NON-NLS-1$
	}
	
	public FindRefsInWorkingSetAction(IWorkbenchSite site){
		this(site,
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.label"), //$NON-NLS-1$
		CSearchMessages.getString("CSearch.FindReferencesInWorkingSetAction.tooltip")); //$NON-NLS-1$
	}
	/**
	 * @param editor
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public FindRefsInWorkingSetAction(CEditor editor, String label, String tooltip) {
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
	public FindRefsInWorkingSetAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		return ICSearchConstants.REFERENCES;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		return CSearchMessages.getFormattedString("WorkingSetScope", CSearchUtil.toString(fWorkingSet)); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScope()
	 */
	protected ICSearchScope getScope() {
		IWorkingSet[] workingSets= fWorkingSet;
		if (fWorkingSet == null) {
			workingSets= CSearchScopeFactory.getInstance().queryWorkingSets();
			if (workingSets == null)
				return null;
			fWorkingSet = workingSets;
		}
		ICSearchScope scope= CSearchScopeFactory.getInstance().createCSearchScope(workingSets);
		CSearchUtil.updateLRUWorkingSets(workingSets);
		
		return scope;
	}
	
	private IWorkingSet[] fWorkingSet;
}
