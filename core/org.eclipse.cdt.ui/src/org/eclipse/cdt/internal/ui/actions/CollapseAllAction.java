/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;

/**
 * Collapse all nodes.
 */
public class CollapseAllAction extends Action {
	
	private final TreeViewer fViewer;
	
	public CollapseAllAction(TreeViewer viewer) {
		super(ActionMessages.CollapseAllAction_label); 
		setDescription(ActionMessages.CollapseAllAction_description); 
		setToolTipText(ActionMessages.CollapseAllAction_tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_COLLAPSE_ALL);
		fViewer = viewer;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	@Override
	public void run() { 
		try {
			fViewer.getControl().setRedraw(false);
			fViewer.collapseAll();
		} finally {
			fViewer.getControl().setRedraw(true);
		}
	}
}
