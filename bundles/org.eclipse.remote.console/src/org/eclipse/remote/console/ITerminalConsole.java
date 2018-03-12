package org.eclipse.remote.console;

import org.eclipse.remote.core.IRemoteConnection;

/**
 * @since 1.2
 */
public interface ITerminalConsole {

	/**
	 * @return The {@link IRemoteConnection} associated to this {@link ITerminalConsole}
	 */
	public IRemoteConnection getConnection();
}
