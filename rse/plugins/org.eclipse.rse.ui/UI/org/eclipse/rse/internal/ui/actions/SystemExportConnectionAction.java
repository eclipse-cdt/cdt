/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - [216858] provide ability to import and export connections
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;

import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;

/**
 * This is the action for clearing in-memory userId and password cache 
 */
public class SystemExportConnectionAction extends SystemBaseAction {
	
	/**
	 * Constructor.
	 */
	public SystemExportConnectionAction() {
		super(SystemResources.RESID_EXPORT_CONNECTIONS_ACTION_LABEL, SystemResources.RESID_EXPORT_CONNECTION_ACTIONS_TOOLTIP, null);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
		setHelp(RSEUIPlugin.HELPPREFIX + "ActionExportConnectionDefinitions"); //$NON-NLS-1$
	}

	/**
	 * The export password action is enabled when the selection contains only connections (hosts)
	 */
	public boolean checkObjectType(Object obj) {
		return true;
	}

	/**
	 * Called when this action is selection from the popup menu.
	 */
	public void run() {
		// TODO DWD implement
	}
}