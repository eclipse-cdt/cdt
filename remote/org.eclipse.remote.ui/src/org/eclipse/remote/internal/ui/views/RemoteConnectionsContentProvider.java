/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.RemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteServicesManager;

public class RemoteConnectionsContentProvider implements ITreeContentProvider, IRemoteConnectionChangeListener {

	private IRemoteServicesManager remoteServicesManager;
	private Viewer viewer;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (newInput instanceof IRemoteServicesManager) {
			if (remoteServicesManager != null) {
				// remove us as a listener on the old manager
				remoteServicesManager.removeRemoteConnectionChangeListener(this);
			}

			remoteServicesManager = (IRemoteServicesManager) newInput;
			remoteServicesManager.addRemoteConnectionChangeListener(this);
		}
	}

	@Override
	public void connectionChanged(RemoteConnectionChangeEvent event) {
		// Refresh the viewer on the UI thread
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public void dispose() {
		if (remoteServicesManager != null) {
			remoteServicesManager.removeRemoteConnectionChangeListener(this);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return remoteServicesManager.getAllRemoteConnections().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		// Connections have no children by default
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IRemoteConnection) {
			return remoteServicesManager;
		} else {
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IRemoteServicesManager) {
			return true;
		} else {
			return false;
		}
	}

}
