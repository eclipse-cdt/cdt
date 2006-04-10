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
/**
 * Suggested interface for actions in popup menus of the remote systems explorer view,
 * which put up dialogs.
 * @see SystemBaseDialogAction
 */
public interface ISystemDialogAction extends ISystemAction
{
	/*
	 * Return the parent window/dialog of this action. Same as getShell()
	 *
    public Shell getParent();*/

	/*
	 * Set the parent window/dialog of this action. Same as setShell(Shell parent)
	 *
    public void setParent(Shell parent);*/
    
	/**
	 * Set the value used as input to the dialog. Usually for update mode.
	 * This is an alternative to selectionChanged or setSelection, as typically it is
	 * the selection that is used as the input to the dialog.
	 */
	public void setValue(Object value); 
	/**
	 * If this action supports allowOnMultipleSelection, then whether the action is to
	 *  be invoked once per selected item (false), or once for all selected items (true)
	 */
	public void setProcessAllSelections(boolean all);   

	/**
	 * Get the output of the dialog. 
	 */
	public Object getValue();
	/**
	 * Returns true if the user cancelled the dialog.
	 * The default way to guess at this is to test if the output from
	 *  getDialogValue was null or not. Override if you need to refine this.
	 */
	public boolean wasCancelled();
}