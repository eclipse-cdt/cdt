/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;

import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemRunAction;
import org.eclipse.swt.widgets.Shell;


/**
 * Adapter class for new connection prompt objects.
 */
public class SystemViewNewConnectionPromptAdapter extends SystemViewPromptableAdapter  {
	
	/**
	 * @see org.eclipse.rse.ui.view.SystemViewPromptableAdapter#getRunAction(org.eclipse.swt.widgets.Shell)
	 */
	protected SystemRunAction getRunAction(Shell shell) {
		return (new SystemRunAction(SystemResources.ACTION_NEWCONN_LABEL, SystemResources.ACTION_NEWCONN_TOOLTIP,
				RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWCONNECTION_ID), shell));
	}
}