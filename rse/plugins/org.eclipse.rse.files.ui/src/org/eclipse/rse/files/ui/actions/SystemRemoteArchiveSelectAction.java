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

package org.eclipse.rse.files.ui.actions;

import org.eclipse.rse.files.ui.dialogs.SystemRemoteArchiveDialog;
import org.eclipse.rse.ui.dialogs.SystemRemoteResourceDialog;
import org.eclipse.swt.widgets.Shell;



public class SystemRemoteArchiveSelectAction extends
		SystemRemoteFileSelectAction
{
	public SystemRemoteArchiveSelectAction(Shell shell)
	{
		super(shell);
	}
	
	public SystemRemoteArchiveSelectAction(Shell shell, String label, String tooltip)
	{
		super(shell, label, tooltip);
	}
	
    protected SystemRemoteResourceDialog createRemoteResourceDialog(Shell shell, String title)
    {
    	return new SystemRemoteArchiveDialog(shell, title);
    }
    
    protected SystemRemoteResourceDialog createRemoteResourceDialog(Shell shell)
    {
    	return new SystemRemoteArchiveDialog(shell);
    }
}