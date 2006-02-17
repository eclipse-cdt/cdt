/*******************************************************************************
 * Copyright (c) 2006 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;


/**
 * @author David Daoust
 *
 */
public class DiscardExternalDefsAction extends IndexAction {
	final IndexView view;
	public DiscardExternalDefsAction(TreeViewer viewer, IndexView view) {
		super(viewer, CUIPlugin.getResourceString("IndexView.ToggleExternals.name")); //$NON-NLS-1$
		this.view = view;
	}
	
	public void run() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return;
		view.toggleExternalDefs();
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
