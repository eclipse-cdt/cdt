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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Doug Schaefer
 *
 */
public class RebuildIndexAction extends IndexAction {
	
	public RebuildIndexAction(TreeViewer viewer) {
		super(viewer, CUIPlugin.getResourceString("IndexView.rebuildIndex.name")); //$NON-NLS-1$
	}
	
	public void run() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return;
		
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i) {
			if (!(objs[i] instanceof ICProject))
				continue;
			
			ICProject project = (ICProject)objs[i];
			IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(project);
			try {
				pdom.getIndexer().reindex();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		}
	}
	
	public boolean valid() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return false;
		Object[] objs = ((IStructuredSelection)selection).toArray();
		for (int i = 0; i < objs.length; ++i)
			if (objs[i] instanceof ICProject)
				return true;
		return false;
	}

}
