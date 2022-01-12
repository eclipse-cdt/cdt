/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

	@Override
	public void destroy() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			controlService.destroy();
		}
	}

	@Override
	public int exitValue() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.exitValue();
		}
		return 0;
	}

	@Override
	public InputStream getErrorStream() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.getErrorStream();
		}
		return null;
	}

	@Override
	public InputStream getInputStream() {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.getInputStream();
		}
		return null;
	}

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

	@Override
	public int waitFor() throws InterruptedException {
		IRemoteProcessControlService controlService = getService(IRemoteProcessControlService.class);
		if (controlService != null) {
			return controlService.waitFor();
		}
		return 0;
	}

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
	@Override
	public IRemoteConnection getRemoteConnection() {
		return connection;
	}

	@Override
	public IRemoteProcessBuilder getProcessBuilder() {
		return builder;
	}

	private RemoteConnectionType getConnectionType() {
		return (RemoteConnectionType) getRemoteConnection().getConnectionType();
	}
}
