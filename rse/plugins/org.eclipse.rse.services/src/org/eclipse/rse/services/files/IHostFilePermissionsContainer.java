/********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.services.files;

/**
 * A container of permissions. Implementations of IHostFile that support
 * IHostFilePermissions should implement this too
 * @since 3.0
 */
public interface IHostFilePermissionsContainer {

	/**
	 * Returns the host file permissions (including user and group) for this file
	 * @return the host file permissions
	 */
	public IHostFilePermissions getPermissions();

	/**
	 * TODO remove this API - here for now because we want to prevent duplicate
	 * query jobs from being triggered from SystemViewRemoteFileAdapter.getPropertyValue()
	 *
	 * Sets the permissions attributes for this file.  Right now, using this
	 * to set a dummy "Pending" set of permissions from UI when doing asynchronous queries
	 * from property sheet or table view
	 */
	public void setPermissions(IHostFilePermissions permissions);

}
