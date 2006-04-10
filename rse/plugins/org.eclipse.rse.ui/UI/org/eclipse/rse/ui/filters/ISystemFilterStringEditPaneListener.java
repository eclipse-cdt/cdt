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

package org.eclipse.rse.ui.filters;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
/**
 * This listener interface is implemented by any code desired to be kept aware
 *  of all user changes to a filter string in the SystemFilterStringEditPane.
 */
public interface ISystemFilterStringEditPaneListener 
{

    /**
     * Callback method. The user has changed the filter string. It may or may not
     *  be valid. If not, the given message is non-null. If it is, and you want it,
     *  call getSystemFilterString() in the edit pane.
     */
    public void filterStringChanged(SystemMessage message);

    /**
     * Callback method. We are about to do a verify,the side effect of which is to
     *  change the current state of the dialog, which we don't want. This tells the
     *  dialog to back up that state so it can be restored.
     */
    public void backupChangedState();
    
    /**
     * Callback method. After backup and change events this is called to restore state
     */
    public void restoreChangedState();
}