/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.core.resources.IProject;

/**
 * A build console event.
 *
 * @see IBuildConsoleListener
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBuildConsoleEvent {
	final static int CONSOLE_START = 1;
	final static int CONSOLE_CLOSE = 2;

	IProject getProject();

	int getType();
}
