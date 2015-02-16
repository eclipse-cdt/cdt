package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.remote.core.IRemoteConnection;

class LaunchTargetTypeInfo {
	public static final String SEP = "|";
	private final String id;
	private final String connectionTypeId;
	private String osname;
	private String osarch;

	public LaunchTargetTypeInfo(IConfigurationElement ce) {
		id = ce.getAttribute("id");
		connectionTypeId = ce.getAttribute("connectionTypeId");
		osname = ce.getAttribute("osname");
		if (osname != null && osname.isEmpty()) {
			osname = null;
		}
		osarch = ce.getAttribute("osarch");
		if (osarch != null && osarch.isEmpty()) {
			osarch = null;
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

		if (osname != null && !osname.equals(connection.getProperty(IRemoteConnection.OS_NAME_PROPERTY))) {
			return false;
		}

		if (osarch != null && !osarch.equals(connection.getProperty(IRemoteConnection.OS_ARCH_PROPERTY))) {
			return false;
		}

		return true;
	}
}
