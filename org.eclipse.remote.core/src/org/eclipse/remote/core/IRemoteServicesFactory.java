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
 * Factory for creating instances of a remote service provider. Implementors must provide a class implementing this interface when
 * supplying a new remote services provider extension.
 */
public interface IRemoteServicesFactory {
	/**
	 * Return the remote services implementation for the given descriptor
	 * 
	 * @param descriptor
	 *            descriptor for the remote services
	 * @return the remote services implementation, or null if initialization failed
	 */
	public IRemoteServices getServices(IRemoteServicesDescriptor descriptor);
}
