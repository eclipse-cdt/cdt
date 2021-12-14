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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for providing connection management operations in the UI. Clients can call these methods to open generic dialogs for
 * operations on connections.
 * @since 2.0
 */
public interface IRemoteUIConnectionService extends IRemoteConnectionType.Service {

	/**
	 * ID for the command to start the new connection wizard.
	 */
	public static final String NEW_CONNECTION_COMMAND = "org.eclipse.remote.ui.command.newConnection"; //$NON-NLS-1$

	/**
	 * Create a wizard for adding or editing connections. The implementation can choose to do this in any way, but typically will
	 * use a dialog or wizard.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @return connection wizard
	 */
	public IRemoteUIConnectionWizard getConnectionWizard(Shell shell);

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
	 */
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, IRemoteConnection connection);

	/**
	 * Return the label provider that provides the text and base image for the connection type
	 * and connections of that type.
	 * 
	 * @return label provider
	 */
	public ILabelProvider getLabelProvider();

}
