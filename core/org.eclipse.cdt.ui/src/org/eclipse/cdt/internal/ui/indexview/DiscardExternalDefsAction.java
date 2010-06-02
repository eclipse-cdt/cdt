/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;


/**
 * @author David Daoust
 *
 */
public class DiscardExternalDefsAction extends IndexAction {
	public DiscardExternalDefsAction(TreeViewer viewer, IndexView view) {
		super(view, viewer, CUIPlugin.getResourceString("IndexView.ToggleExternals.name"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
		setToolTipText(CUIPlugin.getResourceString("IndexView.ToggleExternals.tooltip")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "public_co.gif"); //$NON-NLS-1$	
	}
	
	@Override
	public void run() {
		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return;
		indexView.toggleExternalDefs();
	}
	
	@Override
	public boolean valid() {
		return false;
	}

}
