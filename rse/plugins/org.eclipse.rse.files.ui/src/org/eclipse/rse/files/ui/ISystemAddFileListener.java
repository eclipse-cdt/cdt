/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui;

import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;

/**
 * This is an interface used by the actions SystemSelectRemoteFileAction and SystemSelectRemoteFolderAction
 *  actions classes (and the dialog and form used by these) and enable the dialog's OK button to 
 *  be replaced with an Add button. When the user selects and object or presses Add, the caller
 *  is informed and able to display a resulting message in the dialog's message line,and affect
 *  the enabled state of the Add button.
 * <p>
 * If you call the enableAddButton method you must pass an object that implements this interface.
 * The dialog will call you back when the user presses the Add button, so you can take
 * appropriate action.
 */
public interface ISystemAddFileListener 
{

    /**
     * The user has pressed the Add button. 
     * Do something appropriate with the request.
     * <p>
     * If this action fails for some reason, or you wish to display a completion
     *  message, return message text that will be displayed in the dialog's message 
     *  line. Else, return null.
     */
    public Object addButtonPressed(IHost selectedConnection, IRemoteFile[] selectedObjects);
    /**
     * The user has selected a file or folder. Is this valid to be added? 
     * <p>
     * If so, return null. If not, return a string to display on the 
     * message line indicating why it is not valid, such as it already has
     * been added.
     */
    public Object okToEnableAddButton(IHost selectedConnection, IRemoteFile[] selectedObjects);
}