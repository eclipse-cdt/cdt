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
package org.eclipse.internal.remote.ui.services.local;

import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;

public class LocalUIServices implements IRemoteUIServices {
	private static LocalUIServices fInstance = null;
	private final static LocalUIFileManager fFileMgr = new LocalUIFileManager();

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static LocalUIServices getInstance(IRemoteServices services) {
		if (fInstance == null) {
			fInstance = new LocalUIServices(services);
		}
		return fInstance;
	}

	private final IRemoteServices fServices;

	public LocalUIServices(IRemoteServices services) {
		fServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIServicesDescriptor#getId()
	 */
	public String getId() {
		return fServices.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIServicesDescriptor#getName()
	 */
	public String getName() {
		return fServices.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.ui.IRemoteUIServicesDescriptor#getUIConnectionManager(org.eclipse.remote.core.IRemoteConnectionManager)
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIServicesDescriptor#getUIFileManager(org.eclipse.remote.core.IRemoteConnection)
	 */
	public IRemoteUIFileManager getUIFileManager() {
		return fFileMgr;
	}
}
