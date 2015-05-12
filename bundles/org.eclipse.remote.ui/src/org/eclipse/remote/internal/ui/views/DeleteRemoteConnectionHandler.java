/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteRemoteConnectionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection != null && selection instanceof IStructuredSelection) {
			// Get the manageable connections from the selection
			List<IRemoteConnection> connections = new ArrayList<IRemoteConnection>();
			@SuppressWarnings("unchecked")
			Iterator<Object> i = ((IStructuredSelection) selection).iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				if (obj instanceof IRemoteConnection) {
					IRemoteConnection connection = (IRemoteConnection) obj;
					IRemoteConnectionType connectionType = connection.getConnectionType();
					if (connectionType.canRemove()) {
						connections.add(connection);
					}
				}
			}

			// Confirm the delete
			String message = Messages.DeleteRemoteConnectionHandler_ConfirmDeleteMessage;
			for (IRemoteConnection connection : connections) {
				message += " " + connection.getName(); //$NON-NLS-1$
			}
			message += "?"; //$NON-NLS-1$
			if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					Messages.DeleteRemoteConnectionHandler_DeleteConnectionTitle, message)) {
				for (IRemoteConnection connection : connections) {
					IRemoteConnectionType connectionType = connection.getConnectionType();
					try {
						connectionType.removeConnection(connection);
					} catch (RemoteConnectionException e) {
						RemoteUIPlugin.log(e.getStatus());
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

}
