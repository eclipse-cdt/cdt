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
 * Greg Watson (IBM) - Adapted for telnet service
 *******************************************************************************/
package org.eclipse.remote.telnet.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class TelnetConnectionsUI extends AbstractRemoteUIConnectionService {

	private final IRemoteConnectionType connectionType;

	private TelnetConnectionsUI(IRemoteConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new TelnetConnectionsUI(connectionType);
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
		return new TelnetConnectionWizard(shell, connectionType);
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		try {
			IRunnableWithProgress op = monitor -> {
				try {
					connection.open(monitor);
				} catch (RemoteConnectionException e) {
					throw new InvocationTargetException(e);
				}
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			};
			if (context != null) {
				context.run(true, true, op);
			} else {
				new ProgressMonitorDialog(shell).run(true, true, op);
			}
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
