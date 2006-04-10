/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemUpdateConnectionDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Change Connection dialog
 * THIS DIALOG AND ITS ACTION ARE NO LONGER USED. THEY ARE REPLACED WITH A PROPERTIES DIALOG.
 */
public class SystemUpdateConnectionAction extends SystemBaseDialogAction 
                                 
{
	
	/**
	 * Constructor for SystemUpdateConnectionAction
	 */
	public SystemUpdateConnectionAction(Shell parent) 
	{
		super(SystemResources.ACTION_UPDATECONN_LABEL, SystemResources.ACTION_UPDATECONN_TOOLTIP, null, parent);
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);  
	}

    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		return (selectedObject instanceof IHost);
	}
	
	/**
	 * If you decide to use the supplied run method as is,
	 *  then you must override this method to create and return
	 *  the dialog that is displayed by the default run method
	 *  implementation.
	 * <p>
	 * If you override run with your own, then
	 *  simply implement this to return null as it won't be used.
	 * @see #run()
	 */
	protected Dialog createDialog(Shell parent)
	{
		return new SystemUpdateConnectionDialog(parent);
	}
	
	/**
	 * Required by parent but we do not use it so return null;
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		return null;
	}
}