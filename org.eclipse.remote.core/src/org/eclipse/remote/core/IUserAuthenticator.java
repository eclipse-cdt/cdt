/*******************************************************************************
 * Copyright (c) 2000, 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Atsuhiko Yamanaka, JCraft,Inc. - adding promptForKeyboradInteractive method
 *                                    - copying this class from cvs.core plug-in.
 *     Greg Watson, IBM - modified for use with PTP
 *******************************************************************************/
package org.eclipse.remote.core;

import java.net.PasswordAuthentication;

/**
 * Allow clients to provide their own user interface for connection authentication.
 * 
 * @since 7.0
 */
public interface IUserAuthenticator {

	/**
	 * ID for an "Ok" response (value 0).
	 */
	public final static int OK = 0;

	/**
	 * ID for a "Cancel" response (value 1).
	 */
	public final static int CANCEL = 1;

	/**
	 * ID for a "Yes" response (value 2).
	 */
	public final static int YES = 2;

	/**
	 * ID for a "No" response (value 3).
	 */
	public final static int NO = 3;

	/**
	 * Constant for a prompt with no type (value 0).
	 */
	public final static int NONE = 0;

	/**
	 * Constant for an error prompt (value 1).
	 */
	public final static int ERROR = 1;

	/**
	 * Constant for an information prompt (value 2).
	 */
	public final static int INFORMATION = 2;

	/**
	 * Constant for a question prompt (value 3).
	 */
	public final static int QUESTION = 3;

	/**
	 * Constant for a warning dialog (value 4).
	 */
	public final static int WARNING = 4;

	/**
	 * Authenticates the user for access.
	 * The obtained values for user name and password will be placed
	 * into returned object. Implementors are allowed to
	 * save user names and passwords. The user should be prompted for
	 * user name and password if there is no saved one.
	 * 
	 * @param username
	 *            The initial username, or null if there is no initial username
	 * @param message
	 *            An optional message to display if, e.g., previous authentication failed.
	 */
	public PasswordAuthentication prompt(String username, String message);

	/**
	 * Prompts the user for a number values using text fields. The labels are provided in
	 * the <core>prompt</code> array. Implementors will return the entered values, or null if
	 * the user cancels the prompt.
	 * 
	 * @param destination
	 *            the destination in the format like username@hostname:port
	 * @param name
	 *            a name for this dialog
	 * @param message
	 *            the message to be displayed to the user
	 * @param prompt
	 *            labels for each of the text fields.
	 * @param echo
	 *            an array to show which fields are secret
	 * @return the entered values, or null if the user canceled.
	 */
	public String[] prompt(String destination, String name, String message, String[] prompt, boolean[] echo);

	/**
	 * Prompts for additional information regarding this authentication
	 * request. A default implementation of this method should return the <code>defaultResponse</code>,
	 * whereas alternate implementations could prompt the user with a dialog.
	 * 
	 * @param promptType
	 *            one of the following values:
	 *            <ul>
	 *            <li> <code>NONE</code> for a unspecified prompt type</li>
	 *            <li> <code>ERROR</code> for an error prompt</li>
	 *            <li> <code>INFORMATION</code> for an information prompt</li>
	 *            <li> <code>QUESTION </code> for a question prompt</li>
	 *            <li> <code>WARNING</code> for a warning prompt</li>
	 *            </ul>
	 * @param title
	 *            the prompt title that could be displayed to the user
	 * @param message
	 *            the message to display to the user
	 * @param promptResponses
	 *            the possible responses to the prompt (e.g. corresponding to buttons on a dialog)
	 * @param defaultResponseIndex
	 *            the default response to the prompt
	 * @return the response to the prompt
	 */
	public int prompt(int promptType, String title, String message, int[] promptResponses, int defaultResponseIndex);
}
