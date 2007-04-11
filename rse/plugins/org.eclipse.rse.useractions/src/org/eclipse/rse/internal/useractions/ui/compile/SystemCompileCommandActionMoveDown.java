package org.eclipse.rse.internal.useractions.ui.compile;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDAResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;

/**
 * The action is used within the Work With Compile Commands dialog, in the context menu of the selected compile command.
 * It is used to move the selected compile command up by one in the list.
 */
public class SystemCompileCommandActionMoveDown extends SystemBaseAction {
	private SystemWorkWithCompileCommandsDialog parentDialog;

	/**
	 * Constructor
	 */
	public SystemCompileCommandActionMoveDown(SystemWorkWithCompileCommandsDialog parentDialog) {
		super(SystemUDAResources.RESID_WWCOMPCMDS_ACTION_MOVEDOWN_LABEL, SystemUDAResources.RESID_WWCOMPCMDS_ACTION_MOVEDOWN_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptor(
				ISystemIconConstants.ICON_SYSTEM_MOVEDOWN_ID), null);
		allowOnMultipleSelection(false);
		this.parentDialog = parentDialog;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORDER);
		setHelp(RSEUIPlugin.HELPPREFIX + "wwcc5000"); //$NON-NLS-1$
	}

	/**
	 * We override from parent to do unique checking.
	 * We intercept to ensure this is isn't the last filter string in the list
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		return parentDialog.canMoveDown();
	}

	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() {
		parentDialog.doMoveDown();
	}
}
