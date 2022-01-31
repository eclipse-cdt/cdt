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

import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.internal.ui.RemoteUIPlugin;
import org.eclipse.ui.navigator.CommonNavigator;

public class RemoteConnectionsView extends CommonNavigator {

	@Override
	protected Object getInitialInput() {
		// the remote services manager is the root object
		return RemoteUIPlugin.getService(IRemoteServicesManager.class);
	}

}
