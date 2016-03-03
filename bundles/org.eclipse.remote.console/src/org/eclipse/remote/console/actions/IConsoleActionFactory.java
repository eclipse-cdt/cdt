/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public ConsoleAction createAction(String actionId, String connectionType, IAdaptable adapter);
}
