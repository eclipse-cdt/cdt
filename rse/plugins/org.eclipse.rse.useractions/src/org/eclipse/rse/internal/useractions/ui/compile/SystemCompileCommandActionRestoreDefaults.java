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
 * The action is used within the Work With Compile Commands dialog, in the context menu 
 *   of the selected compile command.
 * It is used to restore shipped defaults of the selected IBM-supplied compile command.
 */
public class SystemCompileCommandActionRestoreDefaults extends SystemBaseAction {
	private SystemWorkWithCompileCommandsDialog parentDialog;

	/**
	 * Constructor 
	 */
	public SystemCompileCommandActionRestoreDefaults(SystemWorkWithCompileCommandsDialog parentDialog) {
		super(SystemUDAResources.RESID_WWCOMPCMDS_ACTION_RESTORE_LABEL, SystemUDAResources.RESID_WWCOMPCMDS_ACTION_RESTORE_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptorFromIDE(
				ISystemIconConstants.ICON_IDE_REFRESH_ID), null);
		allowOnMultipleSelection(false);
		this.parentDialog = parentDialog;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);
		setHelp(RSEUIPlugin.HELPPREFIX + "wwcc6000"); //$NON-NLS-1$
	}

	/**
	 * We override from parent to do unique checking.
	 * We intercept to ensure this is an IBM-supplied compile command
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		return parentDialog.canRestore();
	}

	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() {
		parentDialog.doRestore();
	}
}
