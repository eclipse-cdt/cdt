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

package org.eclipse.rse.ui;
import org.eclipse.jface.viewers.ISelectionProvider;


/**
 * Any UI part that supports the common rename action can implement
 * this to enable the rename popup menu action, supplied by the system explorer view.
 */
public interface ISystemRenameTarget extends ISelectionProvider
{
    /**
     * Return true if rename should even be shown in the popup menu
     */
    public boolean showRename();
    /**
     * Return true if rename should be enabled based on your current selection.
     */
    public boolean canRename();
    /**
     * Actually do the rename of currently selected items.
     * The array of new names matches the currently selected items.
     * Return true if it worked. Return false if it didn't (you display msg), or throw an exception (framework displays msg)
     */
    public boolean doRename(String[] newNames);
}