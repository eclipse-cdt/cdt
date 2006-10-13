/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.search.PDOMSearchBindingQuery;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;

/**
 * @author Doug Schaefer
 *
 */
public class FindDeclarationsAction extends IndexAction {

	public FindDeclarationsAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.findDeclarations.name")); //$NON-NLS-1$
	}
	
	private IIndexBinding getBinding() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		return (objs.length == 1 && objs[0] instanceof IIndexBinding)
			? (IIndexBinding)objs[0] : null;
	}
	
	public void run() {
		IIndexBinding binding = getBinding();
		PDOMSearchBindingQuery query = new PDOMSearchBindingQuery(
				null,
				binding,
				PDOMSearchQuery.FIND_DECLARATIONS | PDOMSearchQuery.FIND_DEFINITIONS);
		
		NewSearchUI.activateSearchResultView();
		
		NewSearchUI.runQueryInBackground(query);
	}
	
	public boolean valid() {
		return getBinding() != null;
	}

}
