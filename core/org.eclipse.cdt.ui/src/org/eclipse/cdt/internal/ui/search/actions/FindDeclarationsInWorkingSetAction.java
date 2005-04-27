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
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindDeclarationsInWorkingSetAction extends FindAction {

	private IWorkingSet[] fWorkingSet;
	private String scopeDescription = "";
	/**
	 * @param site
	 */
	public FindDeclarationsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] wset) {
		this(site,
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (wset != null)
			fWorkingSet = wset;
	}

	/**
	 * @param editor
	 */
	public FindDeclarationsInWorkingSetAction(CEditor editor, IWorkingSet[] wset) {
		this(editor,
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.label"), //$NON-NLS-1$
				CSearchMessages.getString("CSearch.FindDeclarationsInWorkingSetAction.tooltip")); //$NON-NLS-1$
		
		if (wset != null)
			fWorkingSet = wset;
	}

	public FindDeclarationsInWorkingSetAction(CEditor editor, String label, String tooltip){
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
	public FindDeclarationsInWorkingSetAction(IWorkbenchSite site,String label, String tooltip){
		super(site);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		return scopeDescription;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScope(org.eclipse.core.resources.IProject)
	 */
	protected ICSearchScope getScope() {
		IWorkingSet[] workingSets= null;
		if (fWorkingSet == null) {
			workingSets= CSearchScopeFactory.getInstance().queryWorkingSets();
			if (workingSets == null)
				return null;
		}
		else {
			workingSets = fWorkingSet;
		}
		ICSearchScope scope= CSearchScopeFactory.getInstance().createCSearchScope(workingSets);
		scopeDescription = CSearchMessages.getFormattedString("WorkingSetScope", new String[] {CSearchUtil.toString(workingSets)}); //$NON-NLS-1$
		CSearchUtil.updateLRUWorkingSets(workingSets);
		
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		return ICSearchConstants.DECLARATIONS;
	}

}
