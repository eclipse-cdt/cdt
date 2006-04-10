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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to collapse the entire Remote Systems Explorer tree view.
 */
public class SystemCollapseAllAction extends SystemBaseAction 
                                 //
{
	
    // See defect 41203
	
	/**
	 * Constructor
	 */
	public SystemCollapseAllAction(Shell parent) 
	{
		super(SystemResources.ACTION_COLLAPSE_ALL_LABEL, SystemResources.ACTION_COLLAPSE_ALL_TOOLTIP,
				SystemPlugin.getDefault().getImageDescriptorFromIDE(ISystemIconConstants.ICON_IDE_COLLAPSEALL_ID), // D54577
		      	parent);
		setHoverImageDescriptor(SystemPlugin.getDefault().getImageDescriptorFromIDE("elcl16/collapseall.gif")); //$NON-NLS-1$		      	
        allowOnMultipleSelection(true);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_EXPAND); // should never be used       
        setSelectionSensitive(false);
        
		setHelp(SystemPlugin.HELPPREFIX+"actn0023");
		setAccelerator(SWT.CTRL | '-');
	}

	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		return true;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		if ((viewer != null) && (viewer instanceof ISystemResourceChangeListener))
		{			
		  sr.fireEvent((ISystemResourceChangeListener)viewer,
		               new SystemResourceChangeEvent("false", 
		                    ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null));
		}
		else
		  sr.fireEvent(new SystemResourceChangeEvent("false", ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL, null));
	}		
}