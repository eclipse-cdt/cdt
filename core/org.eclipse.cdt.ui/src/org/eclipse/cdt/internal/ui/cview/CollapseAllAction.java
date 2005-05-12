/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		super(CViewMessages.getString("CollapseAllAction.label")); //$NON-NLS-1$
		setDescription(CViewMessages.getString("CollapseAllAction.description")); //$NON-NLS-1$
		setToolTipText(CViewMessages.getString("CollapseAllAction.tooltip")); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_COLLAPSE_ALL);
		cview = part;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.COLLAPSE_ALL_ACTION);
	}
 
	public void run() { 
		cview.collapseAll();
	}
}
