/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.remote.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.widgets.Shell;

/**
 * Convenience methods for handling various actions involving IRemoteUIServices.
 * 
 * @see org.eclipse.remote.ui.IRemoteUIServices
 * @see org.eclipse.remote.ui.IRemoteUIFileManager
 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager
 * 
 * @since 5.0
 * 
 */
public class RemoteUIServicesUtils {
	/**
	 * @param shell
	 * @param context
	 * @param connection
	 * @since 7.0
	 */
	public static void openConnectionWithProgress(final Shell shell, IRunnableContext context, final IRemoteConnection connection) {
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

	/**
	 * Used to configure the default host and port in the wizard used for
	 * choosing a resource manager connection.
	 * 
	 * @see org.eclipse.remote.ui.widgets.RemoteConnectionWidget
	 * 
	 * @param connectionWidget
	 *            the widget allowing the user to choose the connection
	 * @param connection
	 *            name of the connection
	 * @throws URISyntaxException
	 */
	public static void setConnectionHints(RemoteConnectionWidget connectionWidget, IRemoteConnection connection)
			throws URISyntaxException {
		Map<String, String> result = new HashMap<String, String>();
		result.put(IRemoteUIConnectionManager.CONNECTION_ADDRESS_HINT, connection.getAddress());
		result.put(IRemoteUIConnectionManager.LOGIN_USERNAME_HINT, connection.getUsername());
		result.put(IRemoteUIConnectionManager.CONNECTION_PORT_HINT, String.valueOf(connection.getPort()));
		String[] hints = new String[result.size()];
		String[] defaults = new String[hints.length];
		int i = 0;
		for (String s : result.keySet()) {
			hints[i] = s;
			defaults[i++] = result.get(s);
		}
		connectionWidget.setHints(hints, defaults);
	}
}
