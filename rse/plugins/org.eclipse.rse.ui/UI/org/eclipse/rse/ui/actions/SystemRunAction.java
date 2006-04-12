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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.ISystemViewRunnableObject;
import org.eclipse.swt.widgets.Shell;


/**
 * The action is for any object that wants a "Run" action in their popup menu.
 * The object must support the ISystemViewRunnableObject interface.
 */
public class SystemRunAction extends SystemBaseAction 
                                 
{
	
	/**
	 * Constructor.
	 */
	public SystemRunAction(Shell shell) 
	{
		this(SystemResources.ACTION_RUN_LABEL, SystemResources.ACTION_RUN_TOOLTIP,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_RUN_ID),
		      shell);
	}
	
	
	/**
	 * Constructor.
	 * @param label
	 * @param tooltip
	 * @param image the image.
	 * @param shell the parent shell.
	 */
	public SystemRunAction(String label, String tooltip, ImageDescriptor image, Shell shell) 
	{
		super(label, tooltip, image, shell);
		init();
	}
	
	/**
	 * Initialize.
	 */
	protected void init() {
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_OPEN);
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0100");		
	}


	/**
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Object selectedObject = getFirstSelection();
		if ((selectedObject == null) || !(selectedObject instanceof ISystemViewRunnableObject))
		  enable = false;
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		Object selectedObject = getFirstSelection();
		if ((selectedObject == null) || !(selectedObject instanceof ISystemViewRunnableObject))
		  return;
		ISystemViewRunnableObject runnable = (ISystemViewRunnableObject)selectedObject;
		runnable.run(getShell());
	}		
}