/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;

/**
 * Expand all nodes.
 */
public class ExpandAllAction extends Action {
	
	private final TreeViewer fViewer;
	
	public ExpandAllAction(TreeViewer viewer) {
		super(ActionMessages.ExpandAllAction_label); 
		setDescription(ActionMessages.ExpandAllAction_description); 
		setToolTipText(ActionMessages.ExpandAllAction_tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_EXPAND_ALL);
		fViewer = viewer;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.EXPAND_ALL_ACTION);
	}
 
	@Override
	public void run() { 
		try {
			fViewer.getControl().setRedraw(false);
			fViewer.expandAll();
		} finally {
			fViewer.getControl().setRedraw(true);
		}
	}
}
