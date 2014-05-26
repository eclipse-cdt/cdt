/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.Platform;

/**
 * @author DSchaefer
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WindowsRegistry {

	private static boolean failed = false;
	private static WindowsRegistry registry;
	
	private WindowsRegistry() {
	}
	
	public static WindowsRegistry getRegistry() {
		if (registry == null && !failed) {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				try {
					System.loadLibrary("winreg"); //$NON-NLS-1$
					registry = new WindowsRegistry();
				} catch (UnsatisfiedLinkError e) {
					failed = true;
					return null;
				}
			} else
				failed = true;
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
	public native String getLocalMachineValue(String subkey, String name);

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
	public native String getLocalMachineValueName(String subkey, int index);
	
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
	public native String getLocalMachineKeyName(String subkey, int index);
	
	/**
	 * Gets the registry value for the subkey of HKEY_CURRENT_USER with the
	 * given name. If problems occur, like the name is not found, null is returned.
	 * 
	 * @param subkey subkey of HKEY_CURRENT_USER
	 * @param name name of the registry value
	 * @return registry value or null if not found
	 */
	public native String getCurrentUserValue(String subkey, String name);

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
	public native String getCurrentUserValueName(String subkey, int index);
	
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
	public native String getCurrentUserKeyName(String subkey, int index);

}
