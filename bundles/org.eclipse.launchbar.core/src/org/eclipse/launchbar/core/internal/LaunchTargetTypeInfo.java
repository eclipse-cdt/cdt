/*******************************************************************************
 * Copyright (c) 2014,2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *     Elena Laskavaia
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.remote.core.IRemoteConnection;

class LaunchTargetTypeInfo {
	private static final String ANY = "";
	private final String id;
	private final String connectionTypeId;
	private String osname;
	private String osarch;

	public LaunchTargetTypeInfo(IConfigurationElement ce) {
		id = ce.getAttribute("id");
		connectionTypeId = ce.getAttribute("connectionTypeId");
		if (id == null || connectionTypeId == null)
			throw new NullPointerException();
		osname = ce.getAttribute("osname");
		if (osname == null) {
			osname = ANY;
		}
		osarch = ce.getAttribute("osarch");
		if (osarch == null) {
			osarch = ANY;
		}
	}

	public String getId() {
		return id;
	}

	public String getRemoteServicesId() {
		return connectionTypeId;
	}

	public String getOsName() {
		return osname;
	}

	public String getOsArch() {
		return osarch;
	}

	public boolean matches(IRemoteConnection connection) {
		if (!connectionTypeId.equals(connection.getConnectionType().getId())) {
			return false;
		}
		if (!osname.isEmpty() && !osname.equals(connection.getProperty(IRemoteConnection.OS_NAME_PROPERTY))) {
			return false;
		}
		if (!osarch.isEmpty() && !osarch.equals(connection.getProperty(IRemoteConnection.OS_ARCH_PROPERTY))) {
			return false;
		}
		return true;
	}
}
