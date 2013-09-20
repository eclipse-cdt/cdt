/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
 * Abstract base class for remote services. Implementors can use this class to provide a default implementation of a remote
 * services provider.
 * 
 * @since 5.0
 */
public abstract class AbstractRemoteServices implements IRemoteServices {

	protected final IRemoteServicesDescriptor fDescriptor;

	public AbstractRemoteServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IRemoteServicesDescriptor o) {
		return fDescriptor.compareTo(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getId()
	 */
	@Override
	public String getId() {
		return fDescriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getName()
	 */
	@Override
	public String getName() {
		return fDescriptor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	@Override
	public String getScheme() {
		return fDescriptor.getScheme();
	}
}