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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.view.ISystemSelectAllTarget;
import org.eclipse.swt.widgets.Shell;



/**
 * The global action that enables select all.
 * For the RSE tree view, we interpret select all to mean select all the
 *  children of the currently selected parent, if applicable.
 */
public class SystemCommonSelectAllAction
       extends SystemBaseAction
       
{
	private ISystemSelectAllTarget target;
	
	/**
	 * Constructor 
	 * @param parent The Shell of the parent UI for this dialog
	 * @param selProvider The viewer that provides the selections
	 * @param target The viewer that is running this action
	 */
	public SystemCommonSelectAllAction(Shell parent, ISelectionProvider selProvider, ISystemSelectAllTarget target)
	{
		//super(RSEUIPlugin.getResourceBundle(), ISystemConstants.ACTION_SELECTALL, null, parent); TODO: XLATE!
		super("Select All", (ImageDescriptor)null, parent);
		setSelectionProvider(selProvider);
		this.target = target;
		allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);		
  	    //setHelp(RSEUIPlugin.HELPPREFIX+"actn0021"); // TODO: ADD HELP!
	}
	
    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean updateSelection(IStructuredSelection selection)
	{
		return target.enableSelectAll(selection);
	}
	
	/**
	 * This is the method called when the user selects this action.
	 */
	public void run() 
	{
		target.doSelectAll(getSelection());
	}
	
}