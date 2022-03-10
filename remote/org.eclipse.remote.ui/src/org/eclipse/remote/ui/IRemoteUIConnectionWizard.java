/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import java.util.Set;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;

/**
 * Interface for creating and editing connections in the UI.
 */
public interface IRemoteUIConnectionWizard extends IWizard {
	/**
	 * Open configuration wizard allowing the user to enter information about a connection. If the user confirms the information is
	 * correct (e.g. selects OK in a dialog) then a working copy of the connection is returned. If the user discards the
	 * information, then null is returned.
	 *
	 * @return connection working copy or null if the wizard is canceled
	 */
	IRemoteConnectionWorkingCopy open();

	/**
	 * Get the connection being edited.
	 *
	 * @return connection being edited
	 * @since 2.0
	 */
	IRemoteConnectionWorkingCopy getConnection();

	/**
	 * Set a connection containing the information to be edited by the wizard. Setting this value overrides the
	 * {@link #setConnectionName(String)} method.
	 *
	 * @param connection
	 *            connection used to initialize the wizard
	 */
	void setConnection(IRemoteConnectionWorkingCopy connection);

	/**
	 * Set the initial name of the connection.
	 *
	 * @param name
	 *            initial connection name
	 */
	void setConnectionName(String name);

	/**
	 * Supply a set of connection names that are invalid. The dialog should display an error if the user trys to select a name from
	 * the set.
	 *
	 * @param names
	 *            set of invalid connections names
	 */
	void setInvalidConnectionNames(Set<String> names);

}
