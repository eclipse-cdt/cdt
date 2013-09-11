/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * Interface representing a remote services provider extension. Clients can use this to find out information about the extension
 * without loading it.
 */
public interface IRemoteServicesDescriptor extends Comparable<IRemoteServicesDescriptor> {
	/**
	 * Get unique ID of this service. Can be used as a lookup key.
	 * 
	 * @return unique ID
	 */
	public String getId();

	/**
	 * Get display name of this service.
	 * 
	 * @return display name
	 */
	public String getName();

	/**
	 * Get the EFS scheme provided by this service.
	 * 
	 * @return display name
	 */
	public String getScheme();
}
