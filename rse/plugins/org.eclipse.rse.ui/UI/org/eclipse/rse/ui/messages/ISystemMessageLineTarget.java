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

package org.eclipse.rse.ui.messages;
/**
 * Implemented by any class that supports being passed an ISystemMessageLine to
 *  target messages to. This is useful in re-usable forms so that the parent dialog
 *  or wizard can pass in "this" in order to allow the form to issue messages.
 */
public interface ISystemMessageLineTarget 
{
    /**
     * Set the message line to use for issuing messages
     */
    public void setMessageLine(ISystemMessageLine msgLine);
    /**
     * Get the message line to use for issuing messages
     */
    public ISystemMessageLine getMessageLine();
}