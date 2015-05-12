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
package org.eclipse.remote.internal.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteProcessControlService;

/**
 * Standard root class for remote processes.
 */
public class RemoteProcess extends Process implements IRemoteProcess {
	private final Map<Class<? extends Service>, Service> servicesMap = new HashMap<>();
	private final IRemoteConnection connection;
	private final IRemoteProcessBuilder builder;

	/**
	 * @since 2.0
	 */
	public RemoteProcess(IRemoteConnection connection, IRemoteProcessBuilder builder) {
		this.connection = connection;
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			controlService.destroy();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.exitValue();
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.getErrorStream();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.getInputStream();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.getOutputStream();
		}
		return null;
	}

	/**
	 * @since 2.0
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Service> T getService(Class<T> service) {
		T obj = (T) servicesMap.get(service);
		if (obj == null) {
			obj = getConnectionType().getProcessService(this, service);
			if (obj != null) {
				servicesMap.put(service, obj);
			}
		}

		return obj;
	}

	/**
	 * @since 2.0
	 */
	@Override
	public <T extends Service> boolean hasService(Class<T> service) {
		return servicesMap.get(service.getName()) != null || getConnectionType().hasProcessService(service);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.waitFor();
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.isCompleted();
		}
		return true;
	}

	/**
	 * @since 2.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcess#getRemoteConnection()
	 */
	@Override
	public IRemoteConnection getRemoteConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteProcess#getProcessBuilder()
	 */
	@Override
	public IRemoteProcessBuilder getProcessBuilder() {
		return builder;
	}

	private RemoteConnectionType getConnectionType() {
		return (RemoteConnectionType) getRemoteConnection().getConnectionType();
	}
}
