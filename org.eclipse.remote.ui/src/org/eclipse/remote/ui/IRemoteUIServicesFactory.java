/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import org.eclipse.remote.core.IRemoteServices;

/**
 * Factory for creating instances of a remote UI service provider. Implementors must provide a class implementing this interface
 * when supplying a new remote UI services provider extension.
 */
public interface IRemoteUIServicesFactory {
	/**
	 * @param services
	 *            remote services
	 * @return remote services delegate
	 */
	public IRemoteUIServices getServices(IRemoteServices services);
}
