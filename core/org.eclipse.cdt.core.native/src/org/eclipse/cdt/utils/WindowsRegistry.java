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

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * @author DSchaefer
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class WindowsRegistry {

	private static WindowsRegistry registry;

	/**
	 * @since 6.0
	 */
	protected WindowsRegistry() {
	}

	public static WindowsRegistry getRegistry() {
		if (registry == null) {
			try {
				registry = CNativePlugin.getDefault().getWindowsRegistry();
			} catch (CoreException e) {
			}
		}

		return registry;
	}

	/**
	 * Gets the registry value for the subkey of HKEY_LOCAL_MACHINE with the
	 * given name. If problems occur, like the name is not found, null is returned.
	 *
	 * @param subkey subkey of HKEY_LOCAL_MACHINE
	 * @param name name of the registry value
	 * @return registry value or null if not found
	 */
	public abstract String getLocalMachineValue(String subkey, String name);

	/**
	 * Given a subkey of HKEY_LOCAL_MACHINE, and an index (starting from 0)
	 * to the key's array of values, return the name of the indexed value.
	 * The return value is null on any error or when the index is invalid.
	 * The value name can be used in the above getLocalMachineValue() to retrieve
	 * the value data.
	 * @param subkey   subkey of HKEY_LOCAL_MACHINE
	 * @param index    index to the subkey's array of values, starting from 0.
	 * @return name of registry value or null if not found
	 */
	public abstract String getLocalMachineValueName(String subkey, int index);

	/**
	 * Given a subkey of HKEY_LOCAL_MACHINE, and an index (starting from 0)
	 * to the key's array of sub keys, return the name of the indexed key.
	 * The return value is null on any error or when the index is invalid.
	 * The key name can be used in the above getLocalMachineValueName()
	 * to retrieve value names.
	 * @param subkey   subkey of HKEY_CURRENT_USER
	 * @param index    index to the subkey's array of values, starting from 0.
	 * @return name of registry value or null if not found
	 */
	public abstract String getLocalMachineKeyName(String subkey, int index);

	/**
	 * Gets the registry value for the subkey of HKEY_CURRENT_USER with the
	 * given name. If problems occur, like the name is not found, null is returned.
	 *
	 * @param subkey subkey of HKEY_CURRENT_USER
	 * @param name name of the registry value
	 * @return registry value or null if not found
	 */
	public abstract String getCurrentUserValue(String subkey, String name);

	/**
	 * Given a subkey of HKEY_CURRENT_USER, and an index (starting from 0)
	 * to the key's array of values, return the name of the indexed value.
	 * The return value is null on any error or when the index is invalid.
	 * The value name can be used in the above getCurrentUserValue() to retrieve
	 * the value data.
	 * @param subkey   subkey of HKEY_CURRENT_USER
	 * @param index    index to the subkey's array of values, starting from 0.
	 * @return name of registry value or null if not found
	 */
	public abstract String getCurrentUserValueName(String subkey, int index);

	/**
	 * Given a subkey of HKEY_CURRENT_USER, and an index (starting from 0)
	 * to the key's array of sub keys, return the name of the indexed key.
	 * The return value is null on any error or when the index is invalid.
	 * The key name can be used in the above getCurrentUserValueName()
	 * to retrieve value names.
	 * @param subkey   subkey of HKEY_CURRENT_USER
	 * @param index    index to the subkey's array of values, starting from 0.
	 * @return name of registry value or null if not found
	 */
	public abstract String getCurrentUserKeyName(String subkey, int index);
}
