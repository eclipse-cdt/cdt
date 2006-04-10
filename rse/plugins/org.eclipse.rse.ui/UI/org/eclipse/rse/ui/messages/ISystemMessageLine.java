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

import org.eclipse.rse.services.clientserver.messages.SystemMessage;

/**
 * A message line interface. It distinguishs between "normal" messages and errors, as does the
 *  DialogPage classes in eclipse.
 * <p>
 * For each of those, however, we also support both simple string msgs and more robust SystemMessage
 *   messages. A dialog, wizard page or property page class that implements this interface will support
 *   these by using getLevelOneText() to get the string for the first level text, and support mouse
 *   clicking on the message to display the SystemMessageDialog class to show the 2nd level text.
 * <p>
 * Setting an error message hides a currently displayed message until 
 * <code>clearErrorMessage</code> is called.
 */ 
public interface ISystemMessageLine 
{		

	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage();
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage();
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage();

	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage();
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage();

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message);

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message);
	/**
	 * Display the given exception as an error message. This is a convenience
	 * method... a generic SystemMessage is used for exceptions.
	 */
	public void setErrorMessage(Throwable exc);
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message);

	/** 
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message);
		
}