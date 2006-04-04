/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.LimitTo;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchScopeFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class FindRefsProjectAction extends FindAction {

	public FindRefsProjectAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	
	public FindRefsProjectAction(CEditor editor){
		this(editor,
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.tooltip")); //$NON-NLS-1$
	}
	
	public FindRefsProjectAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.tooltip")); //$NON-NLS-1$
	}
	/**
	 * @param site
	 * @param string
	 * @param string2
	 * @param string3
	 */
	public FindRefsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScope(org.eclipse.core.resources.IProject)
	 */
	protected ICSearchScope getScope() {
		
		ICProject proj = null;
		if (fEditor != null){
			proj= fEditor.getInputCElement().getCProject();			 
		} else if (fSite != null){
			IStructuredSelection sel = (IStructuredSelection) getSelection();
			return CSearchScopeFactory.getInstance().createEnclosingProjectScope(sel);
		}
		
		ICElement[] element = new ICElement[1];
		element[0]=proj;
		return SearchEngine.createCSearchScope(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getScopeDescription()
	 */
	protected String getScopeDescription() {
		return CSearchMessages.getString("ProjectScope"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.selsearch.FindAction#getLimitTo()
	 */
	protected LimitTo getLimitTo() {
		return ICSearchConstants.REFERENCES;
	}


}
