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

package org.eclipse.rse.ui.validators;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;


/**
 * Our validators need to support querying max length, for generic rename dialogs.
 * They also need to support getting the message not only as a string, which isValid returns,
 *  but also as a SystemMessage, which getSystemMessage() does if isValid returns non-null.
 */
public interface ISystemValidator extends IInputValidator, ICellEditorValidator
{


    /**
     * Return the max length for this name, or -1 if no max
     */
    public int getMaximumNameLength();
    /**
     * If isValid returns non-null and you desire a full bodied SystemMessage versus a string,
     *  call this method after isValid to get the full bodied SystemMessage.
     * <p>
     * Will be null if isValid returned null.
     */
    public SystemMessage getSystemMessage();
    
    /**
     * For convenience, this is a shortcut to calling:
     * <pre><code>
     *  if (isValid(text) != null)
     *    msg = getSystemMessage();
     * </code></pre>
     */
    public SystemMessage validate(String text);
}