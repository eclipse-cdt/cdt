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

import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.swt.custom.BusyIndicator;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.action.Action;

/**
 * Action enable / disable showing qualified type names
 */
public class ShowQualifiedTypeNamesAction extends Action {

	TypeHierarchyViewPart fView;	
	
	public ShowQualifiedTypeNamesAction(TypeHierarchyViewPart v, boolean initValue) {
		super(TypeHierarchyMessages.getString("ShowQualifiedTypeNamesAction.label")); //$NON-NLS-1$
		setDescription(TypeHierarchyMessages.getString("ShowQualifiedTypeNamesAction.description")); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getString("ShowQualifiedTypeNamesAction.tooltip")); //$NON-NLS-1$
		
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "th_showqualified.gif"); //$NON-NLS-1$
		
		fView= v;
		setChecked(initValue);
		
		WorkbenchHelp.setHelp(this, ICHelpContextIds.SHOW_QUALIFIED_NAMES_ACTION);
	}

	/*
	 * @see Action#actionPerformed
	 */		
	public void run() {
		BusyIndicator.showWhile(fView.getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				fView.showQualifiedTypeNames(isChecked());
			}
		});
	}
}
