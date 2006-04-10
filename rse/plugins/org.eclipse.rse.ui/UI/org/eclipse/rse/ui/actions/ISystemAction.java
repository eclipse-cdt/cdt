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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
/**
 * Suggested interface for actions in popup menus of the remote systems explorer view.
 * While suggested, it is not required to implement this interface.
 * @see SystemBaseAction
 */
public interface ISystemAction extends IAction, ISelectionChangedListener
{

    // ------------------------
    // CONFIGURATION METHODS...
    // ------------------------
	/**
	 * Set the help id for the action
	 */
	public void setHelp(String id);
	/**
	 * Set the context menu group this action is to go into, for popup menus. If not set,
	 *  someone else will make this decision.
	 */
	public void setContextMenuGroup(String group);
	/**
	 * Is this action to be enabled or disabled when multiple items are selected.
	 */
	public void allowOnMultipleSelection(boolean allow);   
	/**
	 * Specify whether this action is selection-sensitive. The default is true.
	 * This means the enabled state is tested and set when the selection is set.
	 */
	public void setSelectionSensitive(boolean sensitive);	

    // -----------------------------------------------------------
    // STATE METHODS CALLED BY VIEWER AT FILL CONTEXT MENU TIME...
    // -----------------------------------------------------------
	/**
	 * Set shell of parent window. Remote systems explorer will call this.
	 */
	public void setShell(Shell shell);
	/**
	 * Set the Viewer that called this action. It is good practice for viewers to call this
	 *  so actions can directly access them if needed.
	 */
	public void setViewer(Viewer v);	
	/**
	 * Sometimes we can't call selectionChanged() because we are not a selection provider.
	 * In this case, use this to set the selection.
	 */
	public void setSelection(ISelection selection);	
    /**
     * An optimization for performance reasons that allows all inputs to be set in one call
     */
    public void setInputs(Shell shell, Viewer v, ISelection selection);	


    // ----------------------------------------------------------------
    // GET METHODS FOR RETRIEVING STATE OR CONFIGURATION INFORMATION...
    // ----------------------------------------------------------------
	/**
	 * Get the help id for this action
	 */
    public String getHelpContextId();
	/**
	 * Convenience method to get shell of parent window, as set via setShell.
	 */
    public Shell getShell();
	/**
	 * Get the Viewer that called this action. Not guaranteed to be set,
	 *  depends if that viewer called setViewer or not. SystemView does.
	 */
	public Viewer getViewer();
	/**
	 * Retrieve selection as set by selectionChanged() or setSelection()
	 */
	public IStructuredSelection getSelection();
	/**
	 * Get the context menu group this action is to go into, for popup menus. By default is
	 *  null, meaning there is no recommendation
	 */
	public String getContextMenuGroup();
	/**
	 * Return whether this action is selection-sensitive. The default is true.
	 * This means the enabled state is tested and set when the selection is set.
	 */
	public boolean isSelectionSensitive();
    /**
     * Return if true if this is a dummy action
     */
    public boolean isDummy();    	
}