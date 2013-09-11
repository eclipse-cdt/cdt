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
package org.eclipse.internal.remote.jsch.ui;

import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;

public class JSchUIServices implements IRemoteUIServices {
	private static JSchUIServices fInstance = null;

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static JSchUIServices getInstance(IRemoteServices services) {
		if (fInstance == null) {
			fInstance = new JSchUIServices(services);
		}
		return fInstance;
	}

	private final IRemoteServices fServices;

	public JSchUIServices(IRemoteServices services) {
		fServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getId()
	 */
	public String getId() {
		return fServices.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getName()
	 */
	public String getName() {
		return fServices.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getUIConnectionManager()
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return new JSchUIConnectionManager(fServices);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getUIFileManager()
	 */
	public IRemoteUIFileManager getUIFileManager() {
		return new JSchUIFileManager(fServices);
	}
}
