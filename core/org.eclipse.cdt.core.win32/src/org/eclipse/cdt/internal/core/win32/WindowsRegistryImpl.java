/*******************************************************************************
 * Copyright (c) 2020 Torbjörn Svensson
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Torbjörn Svensson - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.win32;

import org.eclipse.cdt.utils.WindowsRegistry;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Advapi32Util.EnumKey;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * @author Torbjörn Svensson
 */
public class WindowsRegistryImpl extends WindowsRegistry {

	@Override
	public String getLocalMachineValue(String subkey, String name) {
		return getValue(WinReg.HKEY_LOCAL_MACHINE, subkey, name);
	}

	@Override
	public String getLocalMachineValueName(String subkey, int index) {
		return getValueName(WinReg.HKEY_LOCAL_MACHINE, subkey, index);
	}

	@Override
	public String getLocalMachineKeyName(String subkey, int index) {
		return getKeyName(WinReg.HKEY_LOCAL_MACHINE, subkey, index);
	}

	@Override
	public String getCurrentUserValue(String subkey, String name) {
		return getValue(WinReg.HKEY_CURRENT_USER, subkey, name);
	}

	@Override
	public String getCurrentUserValueName(String subkey, int index) {
		return getValueName(WinReg.HKEY_CURRENT_USER, subkey, index);
	}

	@Override
	public String getCurrentUserKeyName(String subkey, int index) {
		return getKeyName(WinReg.HKEY_CURRENT_USER, subkey, index);
	}

	private String getValue(HKEY key, String subkey, String name) {
		try {
			return Advapi32Util.registryGetStringValue(key, subkey, name);
		} catch (Win32Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getKeyName(HKEY key, String subkey, int index) {
		try {
			HKEYByReference phkKey = Advapi32Util.registryGetKey(key, subkey, WinNT.KEY_READ);
			try {
				EnumKey enumKey = Advapi32Util.registryRegEnumKey(phkKey.getValue(), index);
				return Native.toString(enumKey.lpName);
			} finally {
				Advapi32Util.registryCloseKey(phkKey.getValue());
			}
		} catch (Win32Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getValueName(HKEY key, String subkey, int index) {
		try {
			HKEYByReference phkKey = Advapi32Util.registryGetKey(key, subkey, WinNT.KEY_READ);
			try {
				char[] lpValueName = new char[Advapi32.MAX_KEY_LENGTH];
				IntByReference lpcchValueName = new IntByReference(Advapi32.MAX_KEY_LENGTH);
				int rc = Advapi32.INSTANCE.RegEnumValue(phkKey.getValue(), index, lpValueName, lpcchValueName, null,
						null, null, null);

				if (rc != W32Errors.ERROR_SUCCESS) {
					throw new Win32Exception(rc);
				}

				return Native.toString(lpValueName);
			} finally {
				Advapi32Util.registryCloseKey(phkKey.getValue());
			}
		} catch (Win32Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
