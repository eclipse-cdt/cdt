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
