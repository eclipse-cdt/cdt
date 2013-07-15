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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.internal.remote.ui.RemoteUIPlugin;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.swt.widgets.Shell;

public class LocalUIConnectionManager implements IRemoteUIConnectionManager {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.IRemoteUIConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		MessageDialog.openInformation(shell, Messages.LocalUIConnectionManager_2, Messages.LocalUIConnectionManager_3);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.ui.IRemoteUIConnectionManager#newConnection(org
	 * .eclipse.swt.widgets.Shell, java.lang.String[], java.lang.String[])
	 */
	public IRemoteConnection newConnection(Shell shell, String[] attrHints, String[] attrHintValues) {
		return newConnection(shell);
	}

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
						ErrorDialog.openError(shell, Messages.LocalUIConnectionManager_0, Messages.LocalUIConnectionManager_1,
								new Status(IStatus.ERROR, RemoteUIPlugin.getUniqueIdentifier(), e.getMessage()));
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
				ErrorDialog.openError(shell, Messages.LocalUIConnectionManager_0, Messages.LocalUIConnectionManager_1, new Status(
						IStatus.ERROR, RemoteUIPlugin.getUniqueIdentifier(), e.getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(shell, Messages.LocalUIConnectionManager_0, Messages.LocalUIConnectionManager_1, new Status(
						IStatus.ERROR, RemoteUIPlugin.getUniqueIdentifier(), e.getMessage()));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.ui.IRemoteUIConnectionManager#updateConnection
	 * (org.eclipse.swt.widgets.Shell,
	 * org.eclipse.remote.core.IRemoteConnection)
	 */
	public void updateConnection(Shell shell, IRemoteConnection connection) {
		// TODO Auto-generated method stub

	}
}
