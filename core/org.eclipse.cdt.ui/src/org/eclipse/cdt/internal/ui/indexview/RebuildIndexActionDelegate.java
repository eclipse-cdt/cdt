package org.eclipse.cdt.internal.ui.indexview;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

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

/**
 * @author Doug Schaefer
 *
 */
public class RebuildIndexActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		ISelection selection = targetPart.getSite().getSelectionProvider().getSelection();
		if (!(selection instanceof IStructuredSelection))
			return;
		
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i) {
			if (!(objs[i] instanceof ICProject))
				continue;
			
			ICProject project = (ICProject)objs[i];
			IPDOMIndexer indexer = CCorePlugin.getPDOMManager().getIndexer(project);
			try {
				indexer.reindex();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
