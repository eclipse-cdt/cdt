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
 * A dummy HostFilePermissions node that can be used for deferred
 * query of permissions. Clients who want to read permissions but 
 * see this pending node need to wait until the real permissions
 * get available.
 *
 * @since 3.0
 */
public class PendingHostFilePermissions extends HostFilePermissions {

	public PendingHostFilePermissions()
	{
		super(0, "Pending", "Pending"); //$NON-NLS-2$
	}

}
