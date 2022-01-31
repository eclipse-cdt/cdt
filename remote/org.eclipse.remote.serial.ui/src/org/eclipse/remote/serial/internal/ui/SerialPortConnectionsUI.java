/*******************************************************************************
 * Copyright (c) 2015, 2020 QNX Software Systems, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - initial contribution
 *******************************************************************************/
package org.eclipse.remote.serial.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.serial.ui.NewSerialPortConnectionWizard;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class SerialPortConnectionsUI extends AbstractRemoteUIConnectionService {

	private final IRemoteConnectionType connectionType;

	private SerialPortConnectionsUI(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new SerialPortConnectionsUI(connectionType);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return connectionType;
	}

	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new NewSerialPortConnectionWizard(shell, connectionType);
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		try {
			context.run(false, true, monitor -> {
				try {
					connection.open(monitor);
				} catch (RemoteConnectionException e) {
					Activator.log(e.getStatus());
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new DefaultLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return Activator.getDefault().getImageRegistry().get(Activator.IMG_CONNECTION_TYPE);
			}
		};
	}

}
