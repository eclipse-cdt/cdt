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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action enable / disable member filtering
 */
public class EnableMemberFilterAction extends Action {

	TypeHierarchyViewPart fView;	
	
	public EnableMemberFilterAction(TypeHierarchyViewPart v, boolean initValue) {
		super(TypeHierarchyMessages.getString("EnableMemberFilterAction.label")); //$NON-NLS-1$
		setDescription(TypeHierarchyMessages.getString("EnableMemberFilterAction.description")); //$NON-NLS-1$
		setToolTipText(TypeHierarchyMessages.getString("EnableMemberFilterAction.tooltip")); //$NON-NLS-1$
		
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "impl_co.gif"); //$NON-NLS-1$

		fView= v;
		setChecked(initValue);
		
		WorkbenchHelp.setHelp(this, ICHelpContextIds.ENABLE_METHODFILTER_ACTION);
	}

	/*
	 * @see Action#actionPerformed
	 */		
	public void run() {
		BusyIndicator.showWhile(fView.getSite().getShell().getDisplay(), new Runnable() {
			public void run() {
				fView.enableMemberFilter(isChecked());
			}
		});
	}
}
