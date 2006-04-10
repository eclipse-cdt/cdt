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
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.ui.view.SystemActionViewerFilter;
import org.eclipse.swt.widgets.Shell;


public class SystemRemoteArchiveDialog extends SystemRemoteFileDialog
{
	private SystemActionViewerFilter _filter;
	
	public SystemRemoteArchiveDialog(Shell shell, String title, IHost defaultConnection)
	{
		super(shell, title, defaultConnection);	
	}
	
	public SystemRemoteArchiveDialog(Shell shell, String title)
	{
		super(shell, title);	
		
	}
	
	public SystemRemoteArchiveDialog(Shell shell)
	{
		super(shell, SystemFileResources.RESID_SELECTFILE_TITLE);	
		
	}
		
	public SystemActionViewerFilter getViewerFilter()
	{
		if (_filter== null)
		{
			_filter = new SystemActionViewerFilter();
			Class[] types = {IRemoteFile.class};
			_filter.addFilterCriterion(types, "isDirectory", "true");	
			_filter.addFilterCriterion(types, "isArchive", "true");	
		}
		return _filter;		
	}
	

    public String getVerbage()
    {
    	return SystemFileResources.RESID_SELECTFILE_VERBAGE;
    }
    
    public String getTreeTip()
    {
    	 return SystemFileResources.RESID_SELECTFILE_SELECT_TOOLTIP;
    }
}