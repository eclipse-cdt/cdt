package org.eclipse.rse.internal.useractions.ui.uda;

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
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;

/**
 * The action is used within the Work With User Actions dialog, in the context menu 
 *   of the selected user-defined action.<br>
 * It is used to restore shipped defaults of the selected IBM-supplied user action
 */
public class SystemUDARestoreDefaultsActions extends SystemBaseAction {
	private SystemUDBaseTreeView parentTree;

	/**
	 * Constructor 
	 */
	public SystemUDARestoreDefaultsActions(SystemUDBaseTreeView parentTree) {
		super(SystemUDAResources.RESID_UDA_ACTION_RESTORE_LABEL, SystemUDAResources.RESID_UDA_ACTION_RESTORE_TOOLTIP, RSEUIPlugin.getDefault().getImageDescriptorFromIDE(
				ISystemIconConstants.ICON_IDE_REFRESH_ID), null);
		allowOnMultipleSelection(false);
		this.parentTree = parentTree;
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);
		setHelp(RSEUIPlugin.HELPPREFIX + "udrd0000"); //$NON-NLS-1$
	}

	/**
	 * We override from parent to do unique checking.
	 * We intercept to ensure this is an IBM-supplied compile command
	 */
	public boolean updateSelection(IStructuredSelection selection) {
		return parentTree.canRestore();
	}

	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() {
		parentTree.doRestore();
	}
}
