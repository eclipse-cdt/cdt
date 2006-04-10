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

package org.eclipse.rse.ui.widgets;
import org.eclipse.rse.model.IHost;
/**
 * This is an interface used by the AS400SelectFieldForm, AS400SelectFieldDialog and 
 *  AS400SelectFieldAction classes to enable the caller to be informed when the user
 *  presses the Add button for the selected field.
 * <p>
 * If you call the enableAddButton method you must pass an object that implements this interface.
 * The dialog will call you back when the user presses the Add button, so you can take
 * appropriate action.
 */
public interface ISystemAddListener 
{


    /**
     * The user has pressed the Add button. 
     * Do something appropriate with the request.
     * If this action fails for some reason, or you wish to display a completion
     *  message, return message text that will be displayed in the dialog's message 
     *  line. Else, return null.
     */
    public String addButtonPressed(IHost selectedConnection, Object selectedObject);
    /**
     * The user has selected an object. Is this field valid to be added? 
     * If so, return null. If not, return a string to display on the 
     * message line indicating why it is not valid, such as it already has
     * been added
     */
    public String okToEnableAddButton(IHost selectedConnection, Object selectedObject);
}