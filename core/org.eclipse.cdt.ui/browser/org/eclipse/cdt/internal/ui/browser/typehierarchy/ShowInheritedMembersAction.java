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

import org.eclipse.swt.custom.BusyIndicator;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.Action;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to show / hide inherited members in the method view
 * Depending in the action state a different label provider is installed in the viewer
 */
public class ShowInheritedMembersAction extends Action {
	
	private MethodsViewer fMethodsViewer;
	
	/** 
	 * Creates the action.
	 */
	public ShowInheritedMembersAction(MethodsViewer viewer, boolean initValue) {
		super(TypeHierarchyMessages.getString("ShowInheritedMembersAction.label")); //$NON-NLS-1$
		setDescription(TypeHierarchyMessages.getString("ShowInheritedMembersAction.description")); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getString("ShowInheritedMembersAction.tooltip")); //$NON-NLS-1$
		
//		CPluginImages.setLocalImageDescriptors(this, "inher_co.gif"); //$NON-NLS-1$
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "inher_co.gif"); //$NON-NLS-1$

		fMethodsViewer= viewer;
		
		WorkbenchHelp.setHelp(this, ICHelpContextIds.SHOW_INHERITED_ACTION);
 
		setChecked(initValue);
	}
	
	/*
	 * @see Action#actionPerformed
	 */	
	public void run() {
		BusyIndicator.showWhile(fMethodsViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				fMethodsViewer.showInheritedMethods(isChecked());
			}
		});		
	}	
}
