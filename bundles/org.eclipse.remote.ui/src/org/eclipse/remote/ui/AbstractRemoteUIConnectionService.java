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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.ui.RemoteUIImages;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract base class for providing UI connection manager services.
 * @since 2.0
 */
public abstract class AbstractRemoteUIConnectionService implements IRemoteUIConnectionService {

	@Override
	public void openConnectionWithProgress(final Shell shell, IRunnableContext context, final IRemoteConnection connection) {
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

	protected static class DefaultLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof IRemoteConnection) {
				return ((IRemoteConnection) element).getName();
			} else if (element instanceof IRemoteConnectionType) {
				return ((IRemoteConnectionType) element).getName();
			} else {
				return super.getText(element);
			}
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof IRemoteConnection || element instanceof IRemoteConnectionType) {
				return RemoteUIImages.get(RemoteUIImages.IMG_DEFAULT_TYPE);
			}
			return super.getImage(element);
		}
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new DefaultLabelProvider();
	}

}
