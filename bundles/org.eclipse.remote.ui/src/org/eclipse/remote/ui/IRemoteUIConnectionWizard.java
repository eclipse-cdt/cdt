/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import java.util.Set;

import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * Interface for creating and editing connections in the UI.
 */
public interface IRemoteUIConnectionWizard {
	/**
	 * Open configuration wizard allowing the user to enter information about a connection. If the user confirms the information is
	 * correct (e.g. selects OK in a dialog) then a working copy of the connection is returned. If the user discards the
	 * information, then null is returned.
	 * 
	 * @return connection working copy or null if the wizard is canceled
	 */
	public IRemoteConnectionWorkingCopy open();

	/**
	 * Set a connection containing the information to be edited by the wizard. Setting this value overrides the
	 * {@link #setConnectionName(String)} method.
	 * 
	 * @param connection
	 *            connection used to initialize the wizard
	 */
	public void setConnection(IRemoteConnectionWorkingCopy connection);

	/**
	 * Set the initial name of the connection.
	 * 
	 * @param name
	 *            initial connection name
	 */
	public void setConnectionName(String name);

	/**
	 * Supply a set of connection names that are invalid. The dialog should display an error if the user trys to select a name from
	 * the set.
	 * 
	 * @param names
	 *            set of invalid connections names
	 */
	public void setInvalidConnectionNames(Set<String> names);
}
