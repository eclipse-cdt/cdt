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

package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.ui.dialogs.SystemRemoteResourceDialog;
import org.eclipse.rse.ui.view.SystemActionViewerFilter;
import org.eclipse.swt.widgets.Shell;


public class SystemRemoteFileDialog extends SystemRemoteResourceDialog
{
	public SystemRemoteFileDialog(Shell shell, String title, IHost defaultConnection)
	{
		super(shell, title, new SystemRemoteFileSelectionInputProvider(defaultConnection));	
	}
	
	public SystemRemoteFileDialog(Shell shell, String title)
	{
		super(shell, title, new SystemRemoteFileSelectionInputProvider());	
	}
	
	public SystemRemoteFileDialog(Shell shell)
	{
		super(shell, SystemFileResources.RESID_SELECTFILE_TITLE, new SystemRemoteFileSelectionInputProvider());	
	}
	

    public String getVerbage()
    {
    	return SystemFileResources.RESID_SELECTFILE_VERBAGE;
    }
    
    public String getTreeTip()
    {
    	 return SystemFileResources.RESID_SELECTFILE_SELECT_TOOLTIP;
    }
    
	public SystemActionViewerFilter getViewerFilter()
	{
		return null;
	}
}