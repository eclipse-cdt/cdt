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
package org.eclipse.remote.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.internal.remote.ui.RemoteUIPlugin;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract base class for providing UI connection manager services.
 * 
 * @since 7.0
 */
public abstract class AbstractRemoteUIConnectionManager implements IRemoteUIConnectionManager {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#
	 * openConnectionWithProgress(org.eclipse.swt.widgets.Shell,
	 * org.eclipse.jface.operation.IRunnableContext,
	 * org.eclipse.remote.core.IRemoteConnection)
	 */
	public void openConnectionWithProgress(final Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		if (!connection.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
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
				ErrorDialog.openError(shell, Messages.AbstractRemoteUIConnectionManager_Connection_Error,
						Messages.AbstractRemoteUIConnectionManager_Could_not_open_connection, new Status(IStatus.ERROR,
								RemoteUIPlugin.PLUGIN_ID, e.getCause().getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(shell, Messages.AbstractRemoteUIConnectionManager_Connection_Error,
						Messages.AbstractRemoteUIConnectionManager_Could_not_open_connection, new Status(IStatus.ERROR,
								RemoteUIPlugin.PLUGIN_ID, e.getMessage()));
			}
		}
	}
}
