/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for providing connection management operations in the UI. Clients can call these methods to open generic dialogs for
 * operations on connections.
 */
public interface IRemoteUIConnectionManager {
	/**
	 * @since 5.0
	 */
	public static String CONNECTION_ADDRESS_HINT = "CONNECTION_ADDRESS_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String CONNECTION_PORT_HINT = "CONNECTION_PORT_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String CONNECTION_TIMEOUT_HINT = "CONNECTION_TIMEOUT_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String LOGIN_USERNAME_HINT = "LOGIN_USERNAME_HINT"; //$NON-NLS-1$

	/**
	 * Create a new connection. The implementation can choose to do this in any way, but typically will use a dialog or wizard.
	 * 
	 * Returns a working copy of the remote connection. Callers must call {@link IRemoteConnectionWorkingCopy#save()} before the
	 * connection can be used.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @return newly created remote connection working copy or null if none created
	 */
	public IRemoteConnectionWorkingCopy newConnection(Shell shell);

	/**
	 * Create a new connection using the remote service provider new connection dialog. If attrHints and attrHintValues are provided
	 * then the dialog will attempt to use these values as the default values for the appropriate dialog fields.
	 * 
	 * Returns a working copy of the remote connection. Callers must call {@link IRemoteConnectionWorkingCopy#save()} before the
	 * connection can be used.
	 * 
	 * @param shell
	 *            shell used to display dialog
	 * @param attrHints
	 *            array containing attribute hints
	 * @param attrHintValues
	 *            array containing default values for each attribute specified
	 *            in attrHints
	 * @return the newly created connection working copy or null if none created
	 * @since 5.0
	 */
	public IRemoteConnectionWorkingCopy newConnection(Shell shell, String[] attrHints, String[] attrHintValues);

	/**
	 * Attempt to open a connection using a progress monitor. Can be called on either open or closed connections, and will
	 * initialize the remote services if necessary. Users should check connection.isOpen() on return to determine if the connection
	 * was actually opened.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @param context
	 *            runnable context for displaying progress indicator. Can be null.
	 * @param connection
	 *            connection to open
	 * @since 5.0
	 */
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, IRemoteConnection connection);

	/**
	 * Change a connection configuration. The implementation can chose to do this in any way, but typically will use a dialog or
	 * wizard. Callers must call {@link IRemoteConnectionWorkingCopy#save()} on the working copy for the changes to be saved.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @param connection
	 *            working copy of the connection to modify
	 * @return true if the connection information was changed
	 */
	public boolean updateConnection(Shell shell, IRemoteConnectionWorkingCopy connection);
}
