package org.eclipse.remote.console;

import org.eclipse.remote.core.IRemoteConnection;

public interface ITerminalConsole {

	/**
	 * @return The {@link IRemoteConnection} associated to this {@link ITerminalConsole}
	 */
	public IRemoteConnection getConnection();
}
