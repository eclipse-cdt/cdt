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
package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.workingsets.WorkingSetMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Clears the selected working set in the action group's view.
 * 
 * @since 2.0
 */
public class ClearWorkingSetAction extends Action {
	
	private WorkingSetFilterActionGroup fActionGroup;

	public ClearWorkingSetAction(WorkingSetFilterActionGroup actionGroup) {
		super(WorkingSetMessages.getString("ClearWorkingSetAction.text")); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		setToolTipText(WorkingSetMessages.getString("ClearWorkingSetAction.toolTip")); //$NON-NLS-1$
		setEnabled(actionGroup.getWorkingSet() != null);
		WorkbenchHelp.setHelp(this, ICHelpContextIds.CLEAR_WORKING_SET_ACTION);
		fActionGroup= actionGroup;
	}

	/*
	 * Overrides method from Action
	 */
	public void run() {
		fActionGroup.setWorkingSet(null, true);
	}
}
