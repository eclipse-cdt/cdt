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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.SystemPerspectiveLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenInNewWindowAction;


/**
 * The action allows users to open a new Remote Systems Explorer perspective, anchored
 *  by the currently selected resource.
 */
public class SystemOpenExplorerPerspectiveAction 
       extends SystemBaseAction 
       
{
	//private boolean replaceEnabled = true;
	private IWorkbenchWindow window;
	private IPerspectiveRegistry reg;
	private IPerspectiveDescriptor desc = null;
	
	/**
	 * Constructor 
	 */
	public SystemOpenExplorerPerspectiveAction(Shell parent, IWorkbenchWindow currentWorkbenchWindow) 
	{
		super(SystemResources.ACTION_OPENEXPLORER_DIFFPERSP2_LABEL, SystemResources.ACTION_OPENEXPLORER_DIFFPERSP2_TOOLTIP, 
				RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PERSPECTIVE_ID),
				parent);
		this.window = currentWorkbenchWindow;
		this.reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	    this.desc = reg.findPerspectiveWithId(SystemPerspectiveLayout.ID);
		
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_OPEN);  
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0016"); 
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We intercept to ensure only connections from the same profile are selected.
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Object selected = selection.getFirstElement();
		if (selected instanceof ISystemFilterReference)
		{
		  if ( ((ISystemFilterReference)selected).getReferencedFilter().isPromptable() )
		    enable = false;
		}
		else if (selected instanceof ISystemPromptableObject)
		  enable = false;
		return enable;
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see Action#run()
	 */
	public void run() 
	{
		/* OLD RELEASE 1 CODE
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		String perspectiveSetting =
			store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
		runWithPerspectiveValue(desc, perspectiveSetting);		
		*/
		OpenInNewWindowAction workbenchOpenAction = // NEW FOR RELEASE 2
		   new OpenInNewWindowAction(window,getPageInput());		
		workbenchOpenAction.run();
	}		
	
	/**
	 * Sets the page input.  
	 *
	 * @param input the page input
	 */
	public void setPageInput(IAdaptable input) 
	{
	}
	/**
	 * Get the page input.
	 * Will use explicitly set input if given, else deduces from selection
	 */
	public IAdaptable getPageInput()
	{
		//if (pageInput != null) safer to always recalculate!
		//  return pageInput;
		//else
		{
			Object firstSel = getFirstSelection();
			if ((firstSel != null) && (firstSel instanceof IAdaptable))
			  return (IAdaptable)firstSel;
			else
			  return null;
		}
	}
	
}