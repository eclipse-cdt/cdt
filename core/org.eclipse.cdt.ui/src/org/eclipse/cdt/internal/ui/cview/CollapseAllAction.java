/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * Collapse all nodes.
 */
class CollapseAllAction extends Action {
	
	private CView cview;
	
	CollapseAllAction(CView part) {
		super(CViewMessages.CollapseAllAction_label); 
		setDescription(CViewMessages.CollapseAllAction_description); 
		setToolTipText(CViewMessages.CollapseAllAction_tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_COLLAPSE_ALL);
		cview = part;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	@Override
	public void run() { 
		cview.collapseAll();
	}
}
