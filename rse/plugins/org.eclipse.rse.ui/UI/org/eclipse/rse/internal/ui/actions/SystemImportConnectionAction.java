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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;

/**
 * This action is used to import a connection from its package file into the default profile.
 */
public class SystemImportConnectionAction extends SystemBaseAction {

	private static final SimpleSystemMessage error1 = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, IStatus.ERROR, SystemResources.RESID_IMPORT_CONNECTION_ACTION_INVALID_FORMAT);
	private static final SimpleSystemMessage error2 = new SimpleSystemMessage(RSEUIPlugin.PLUGIN_ID, IStatus.ERROR, SystemResources.RESID_IMPORT_CONNECTION_ACTION_READER_MISSING);
	
	/**
	 * Creates a new action to import a connection into a profile.
	 */
	public SystemImportConnectionAction() {
		super(SystemResources.RESID_IMPORT_CONNECTION_ACTION_LABEL, SystemResources.RESID_IMPORT_CONNECTION_ACTION_TOOLTIP, null);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
		setHelp(RSEUIPlugin.HELPPREFIX + "ActionImportConnectionDefinitions"); //$NON-NLS-1$
	}

	/**
	 * The import action can be run no matter what the selection is.
	 * @return true
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