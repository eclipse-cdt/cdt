/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import java.util.List;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for providing file management operations in the UI. Clients can call these methods to open generic dialogs for
 * operations on remote resources.
 *
 * @since 2.0
 */
public interface IRemoteUIFileService extends IRemoteConnectionType.Service {
	/**
	 * Browse for a remote directory. The return value is the path of the
	 * directory <i>on the remote system</i>.
	 *
	 * Equivalent to {@link org.eclipse.swt.widgets.DirectoryDialog}.
	 *
	 * @param shell
	 *            workbench shell
	 * @param message
	 *            message to display in dialog
	 * @param initialPath
	 *            initial path to use when displaying files
	 * @param flags
	 *            option settings for dialog (not currently used)
	 *            valid values are NONE (@see IRemoteUIConstants)
	 * @return the path to the directory relative to the remote system or null
	 *         if the browser was cancelled
	 */
	public String browseDirectory(Shell shell, String message, String initialPath, int flags);

	/**
	 * Browse for a remote file. The return value is the path of the file <i>on
	 * the remote system</i>.
	 *
	 * Equivalent to {@link org.eclipse.swt.widgets.FileDialog}.
	 *
	 * @param shell
	 *            workbench shell
	 * @param message
	 *            message to display in dialog
	 * @param initialPath
	 *            initial path to use when displaying files
	 * @param flags
	 *            options settings for dialog
	 *            valid values are NONE, SAVE, or OPEN (@see IRemoteUIConstants)
	 * @return the path to the file relative to the remote system or null if the
	 *         browser was cancelled
	 */

	public String browseFile(Shell shell, String message, String initialPath, int flags);

	/**
	 * Browse for a set of remote files. The return value is a list of paths
	 * of the files <i>on the remote system</i>.
	 *
	 * Equivalent to {@link org.eclipse.swt.widgets.FileDialog}.
	 *
	 * @param shell
	 *            workbench shell
	 * @param message
	 *            message to display in dialog
	 * @param initialPath
	 *            initial path to use when displaying files
	 * @param flags
	 *            options settings for dialog (@see IRemoteUIConstants)
	 *            valid values are NONE, SAVE, or OPEN (@see IRemoteUIConstants)
	 * @return list of paths to the files relative to the remote system or null if the
	 *         browser was cancelled
	 */
	public List<String> browseFiles(Shell shell, String message, String initialPath, int flags);

	/**
	 * Get the last connection that was selected in the browser.
	 *
	 * @return selected connection
	 */
	public IRemoteConnection getConnection();

	/**
	 * Set the connection to use for file browsing. The connection must support the IRemoteFileService service or it will be
	 * ignored.
	 *
	 * @param connection
	 *            connection to use for file browsing
	 */
	public void setConnection(IRemoteConnection connection);

	/**
	 * Show a list of available connections if possible.
	 *
	 * @param enable
	 *            enable connection list
	 */
	public void showConnections(boolean enable);
}
