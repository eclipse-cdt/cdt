/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Kevin Doyle (IBM) - Added Double Click Listener that closes dialog on file double click
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [190442] made SystemActionViewerFilter API
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types
 *******************************************************************************/

package org.eclipse.rse.files.ui.dialogs;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.internal.files.ui.view.SystemRemoteFileSelectionInputProvider;
import org.eclipse.rse.internal.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.ui.SystemActionViewerFilter;
import org.eclipse.rse.ui.dialogs.SystemRemoteResourceDialog;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	

    public String getVerbiage()
    {
    	return SystemFileResources.RESID_SELECTFILE_VERBIAGE;
    }
    
    public String getTreeTip()
    {
    	 return SystemFileResources.RESID_SELECTFILE_SELECT_TOOLTIP;
    }
    
	public SystemActionViewerFilter getViewerFilter()
	{
		return null;
	}
	
	/**
	 * Override of parent.
	 */
	protected Control createContents(Composite parent) 
	{
		Control control = super.createContents(parent);
		getSystemTree().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});    		
		return control;
	}
	
	/**
	 * Handles double clicks in viewer.
	 * Closes the dialog if a file is double clicked
	 */
	protected void handleDoubleClick(DoubleClickEvent event) 
	{
		ISystemTree tree = getSystemTree();
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null)
			return;
		if (isPageComplete() && !tree.isExpandable(element))
		{
			setReturnCode(OK);
			if (processOK())
		    {
			  	okPressed = true;
			    close();
			}
		}
	}
}
