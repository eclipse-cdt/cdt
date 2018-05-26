package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import java.util.Collection;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagDeviceImpl;

public class GenericSerialNoExtendedRemoteInfo extends DefaultGDBJtagDeviceImpl
		implements IGDBJtagConnection {

	private String connection;

	@Override
	public void setDefaultDeviceConnection(String connection) {
		this.connection = connection;
	}

	@Override
	public void doRemote(String connection, Collection<String> commands) {
		if (connection != null) {
			addCmd(commands, "-target-select remote " + connection);
		}
	}

	@Override
	public String getDefaultDeviceConnection() {
		return connection;
	}

}
