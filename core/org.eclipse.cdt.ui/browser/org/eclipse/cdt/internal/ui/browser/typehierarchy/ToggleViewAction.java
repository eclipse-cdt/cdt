/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to switch between the different hierarchy views.
 */
public class ToggleViewAction extends Action {
	
	private TypeHierarchyViewPart fViewPart;
	private int fViewerIndex;
		
	public ToggleViewAction(TypeHierarchyViewPart v, int viewerIndex) {
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		String contextHelpId= null;
		if (viewerIndex == TypeHierarchyViewPart.VIEW_ID_SUPER) {
			setText(TypeHierarchyMessages.getString("ToggleViewAction.supertypes.label")); //$NON-NLS-1$
			contextHelpId= ICHelpContextIds.SHOW_SUPERTYPES;
			setDescription(TypeHierarchyMessages.getString("ToggleViewAction.supertypes.description")); //$NON-NLS-1$
			setToolTipText(TypeHierarchyMessages.getString("ToggleViewAction.supertypes.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "super_co.gif"); //$NON-NLS-1$
		} else if (viewerIndex == TypeHierarchyViewPart.VIEW_ID_SUB) {
			setText(TypeHierarchyMessages.getString("ToggleViewAction.subtypes.label")); //$NON-NLS-1$
			contextHelpId= ICHelpContextIds.SHOW_SUBTYPES;
			setDescription(TypeHierarchyMessages.getString("ToggleViewAction.subtypes.description")); //$NON-NLS-1$
			setToolTipText(TypeHierarchyMessages.getString("ToggleViewAction.subtypes.tooltip")); //$NON-NLS-1$
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "sub_co.gif"); //$NON-NLS-1$
		} else {
			Assert.isTrue(false);
		}		
		
		fViewPart= v;
		fViewerIndex= viewerIndex;
		
		WorkbenchHelp.setHelp(this, contextHelpId);
	}
				
	public int getViewerIndex() {
		return fViewerIndex;
	}

	/*
	 * @see Action#actionPerformed
	 */	
	public void run() {
		fViewPart.setView(fViewerIndex);
	}		
}
