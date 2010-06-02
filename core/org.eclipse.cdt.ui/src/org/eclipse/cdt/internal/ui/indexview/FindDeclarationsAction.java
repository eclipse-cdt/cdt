/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;

/**
 * @author Doug Schaefer
 *
 */
public class FindDeclarationsAction extends IndexAction {

	public FindDeclarationsAction(IndexView view, TreeViewer viewer) {
		super(view, viewer, CUIPlugin.getResourceString("IndexView.findDeclarations.name")); //$NON-NLS-1$
	}
	
	private IndexNode getBindingNode() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		if (objs.length == 1 && objs[0] instanceof IndexNode) {
			IndexNode node= (IndexNode) objs[0];
			if (node.fObject instanceof IIndexBinding) {
				return node;
			}
		}
		return null;
	}
	
	@Override
	public void run() {
		IndexNode binding = getBindingNode();
		if (binding != null) {
			ICProject cproject= binding.getProject();
			if (cproject != null) {
				IndexViewSearchQuery query = new IndexViewSearchQuery(
						null,
						cproject, indexView.getLastWriteAccess(cproject),
						(IIndexBinding) binding.fObject, binding.fText,
						PDOMSearchQuery.FIND_DECLARATIONS | PDOMSearchQuery.FIND_DEFINITIONS);

				NewSearchUI.activateSearchResultView();
				NewSearchUI.runQueryInBackground(query);
			}
		}
	}
	
	@Override
	public boolean valid() {
		return getBindingNode() != null;
	}
}
