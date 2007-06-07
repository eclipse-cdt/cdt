/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David McKnight   (IBM)        - [187342] Open in New Window expand failed error when not connected
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.OpenInNewWindowAction;


/**
 * The action allows users to open a new Remote System Explorer perspective, anchored
 *  by the currently selected resource.
 */
public class SystemOpenExplorerPerspectiveAction 
       extends SystemBaseAction 
       
{
	//private boolean replaceEnabled = true;
	private IWorkbenchWindow window;
	
	/**
	 * Constructor 
	 */
	public SystemOpenExplorerPerspectiveAction(Shell parent, IWorkbenchWindow currentWorkbenchWindow) 
	{
		super(SystemResources.ACTION_OPENEXPLORER_DIFFPERSP2_LABEL, SystemResources.ACTION_OPENEXPLORER_DIFFPERSP2_TOOLTIP, 
				RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PERSPECTIVE_ID),
				parent);
		this.window = currentWorkbenchWindow;
		
        allowOnMultipleSelection(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_OPEN);  
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0016");  //$NON-NLS-1$
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
		final IAdaptable input = getPageInput();
		final ISubSystem ss = getSubSystem(input);
		if (ss!=null && !ss.isConnected()) {			
			// Connect if the object's children will require a live connection.  If the connect is made, then
			// the open window occurs as a result of the callback to connect.
			try
			{
				ss.connect(false, 
					new IRSECallback() // call back for opening the window after the connect
					{
						public void done(IStatus status, Object object)
						{
							if (ss.isConnected()) // only open the window if we're connected
							{
								Display.getDefault().asyncExec(new Runnable()
								{
									public void run()
									{
										openWindow(input);
									}
								});
							}
							
						}
					});		
			}
			catch (Exception e) {
				// ignore since connect failed (and don't show in the window)
				SystemBasePlugin.logError(e.getMessage(), e);
			}
		}
		else {
			openWindow(input);
		}
	}		
		
	/**
	 * Returns the associated subsystem if the object is remote, a filter or a subsystem
	 * @param input the input object
	 * @return
	 */	
	private ISubSystem getSubSystem(IAdaptable input)
	{
		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)input.getAdapter(ISystemViewElementAdapter.class);
		if (adapter != null) {
			if ((input instanceof ISystemFilterReference)
				|| input instanceof ISubSystem	
				|| adapter.isRemote(input)) // not sure if we'd ever have 
										// a remote object when not connected
			{
				return adapter.getSubSystem(input);
			}
		}
		return null;
	}

	
	/**
	 * Open the input object in a new window
	 * @param input the input to the new window
	 */
	protected void openWindow(IAdaptable input)
	{
		OpenInNewWindowAction workbenchOpenAction = // NEW FOR RELEASE 2
			   new OpenInNewWindowAction(window,input);
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