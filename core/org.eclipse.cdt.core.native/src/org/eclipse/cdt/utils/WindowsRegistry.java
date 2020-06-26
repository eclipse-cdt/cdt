/*******************************************************************************
 * Copyright (c) 2005, 2014 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import org.eclipse.cdt.core.IWindowsRegistry;

/**
 * @author DSchaefer
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class WindowsRegistry implements IWindowsRegistry {

	private static WindowsRegistry registry;

	/**
	 * @since 6.0
	 */
	protected WindowsRegistry() {
	}

	public static WindowsRegistry getRegistry() {
		if (registry == null) {

		}

		return registry;
	}
}
