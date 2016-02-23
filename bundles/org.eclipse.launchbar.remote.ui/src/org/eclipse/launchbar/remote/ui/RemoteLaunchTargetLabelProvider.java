/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.remote.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.swt.graphics.Image;

public class RemoteLaunchTargetLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchTarget) {
			IRemoteConnection connection = ((ILaunchTarget) element).getAdapter(IRemoteConnection.class);
			if (connection != null) {
				IRemoteUIConnectionService uiService = connection.getConnectionType()
						.getService(IRemoteUIConnectionService.class);
				if (uiService != null) {
					return uiService.getLabelProvider().getText(connection);
				}
			}
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ILaunchTarget) {
			IRemoteConnection connection = ((ILaunchTarget) element).getAdapter(IRemoteConnection.class);
			if (connection != null) {
				IRemoteUIConnectionService uiService = connection.getConnectionType()
						.getService(IRemoteUIConnectionService.class);
				if (uiService != null) {
					return uiService.getLabelProvider().getImage(connection);
				}
			}
		}
		return super.getImage(element);
	}

}
