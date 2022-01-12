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
package org.eclipse.remote.internal.jsch.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteConnectionType.Service;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.ui.messages.Messages;
import org.eclipse.remote.internal.jsch.ui.wizards.JSchConnectionWizard;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class JSchUIConnectionService extends AbstractRemoteUIConnectionService {

	private final IRemoteConnectionType fConnectionType;

	public JSchUIConnectionService(IRemoteConnectionType connectionType) {
		fConnectionType = connectionType;
	}

	public static class Factory implements IRemoteConnectionType.Service.Factory {
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Service> T getService(IRemoteConnectionType connectionType, Class<T> service) {
			if (IRemoteUIConnectionService.class.equals(service)) {
				return (T) new JSchUIConnectionService(connectionType);
			}
			return null;
		}
	}

	@Override
	public IRemoteConnectionType getConnectionType() {
		return fConnectionType;
	}

	@Override
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell) {
		return new JSchConnectionWizard(shell, fConnectionType);
	}

	@Override
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		if (!connection.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connection.open(monitor);
					} catch (RemoteConnectionException e) {
						throw new InvocationTargetException(e);
					}
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			try {
				if (context != null) {
					context.run(true, true, op);
				} else {
					new ProgressMonitorDialog(shell).run(true, true, op);
				}
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(shell, Messages.JSchUIConnectionManager_Connection_Error,
						Messages.JSchUIConnectionManager_Could_not_open_connection,
						new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getCause().getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(shell, Messages.JSchUIConnectionManager_Connection_Error,
						Messages.JSchUIConnectionManager_Could_not_open_connection,
						new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
			}
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
