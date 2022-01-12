/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.console.actions;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @since 1.1
 */
public interface IConsoleActionFactory {
	/**
	 * Returns an implementation of ConsoleAction
	 *
	 * @param actionId
	 * 				The id of the action being requested
	 * @param connectionType
	 * 				The connection type of the terminal console
	 * @param adapter
	 * 				An adapter to get relevant objects for use by the ConsoleAction being created (eg. IRemoteConnection)
	 * @return an implementation of ConsoleAction
	 */
	public ConsoleAction createAction(String actionId, String connectionType, IAdaptable adapter);
}
