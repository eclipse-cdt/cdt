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
import org.eclipse.rse.model.IHost;

/**
 * <p>
 * This interface is used by the remote object selection dialogs when Add mode is enabled.
 * <p>
 * This interface allows you to listen generically for selection events on any remote object,
 * and be called when the user selects something or presses. You can use instanceof to 
 * decide what was selected. 
 * <p>
 * If you call the enableAddButton method you must pass an object that implements this interface.
 * The dialog will call you back when the user presses the Add button, so you can take
 * appropriate action.
 */
public interface IRemoteSelectionAddListener 
{


    /**
     * The user has selected a remote object. Is this object valid to be added? 
     * If so, return null. If not, return a string to display on the 
     * message line indicating why it is not valid, such as it already has
     * been added.
     * 
     * @param selectedConnection The connection the object was selected in
     * @param selectedObjects Will be a list of objects such as AS400Library or IRemoteFile. They are
     *   resolved so that the remote adapter is not required.
     * 
     * @return A String or SystemMessage object that will be displayed if the 
     * action fails, null if the action was successfull
     */
    public Object okToEnableAddButton(IHost selectedConnection, Object[] selectedObjects);

    /**
     * The user has pressed the Add button. 
     * Do something appropriate with the request.
     * If this action fails for some reason, or you wish to display a completion
     *  message, return message text that will be displayed in the dialog's message 
     *  line. Else, return null.
     * 
     * @param selectedConnection The connection the object was selected in
     * @param selectedObjects Will be a list of objects such as AS400Library or IRemoteFile. They are
     *  resolved so that the remote adapter is not required.
     * 
     * @return A String or SystemMessage object that will be displayed if the 
     * action fails, null if the action was successfull
     */
    public Object addButtonPressed(IHost selectedConnection, Object[] selectedObjects);

}