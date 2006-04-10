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

package org.eclipse.rse.ui.view;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This interface is implemented by all viewers that wish to support
 * the global select all action. To do so, they implement this interface,
 * then instantiate SystemCommonSelectAllAction, and call setGlobalActionHandler.
 * See SystemViewPart for an example.
 */
public interface ISystemSelectAllTarget 
{


	/**
	 * Return true if select all should be enabled for the given object.
	 * For a tree view, you should return true if and only if the selected object has children.
	 * You can use the passed in selection or ignore it and query your own selection.
	 */
    public boolean enableSelectAll(IStructuredSelection selection);
    /**
     * When this action is run via Edit->Select All or via Ctrl+A, perform the
     * select all action. For a tree view, this should select all the children 
     * of the given selected object. You can use the passed in selected object
     * or ignore it and query the selected object yourself. 
     */
    public void doSelectAll(IStructuredSelection selection);
}