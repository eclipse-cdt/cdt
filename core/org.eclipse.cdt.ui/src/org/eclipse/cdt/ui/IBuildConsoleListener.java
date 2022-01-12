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

/**
 * A listener to build console events.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IBuildConsoleEvent
 */
public interface IBuildConsoleListener {
	void consoleChange(IBuildConsoleEvent event);
}
