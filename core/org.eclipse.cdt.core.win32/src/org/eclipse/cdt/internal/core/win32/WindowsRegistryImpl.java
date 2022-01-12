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

import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.internal.core.natives.CNativePlugin;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
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

	private final static boolean DEBUG = Platform.getDebugBoolean(CNativePlugin.PLUGIN_ID + "/debug/win32/registry"); //$NON-NLS-1$

	@Override
	public String getLocalMachineValue(String subkey, String name) {
		return getValue(WinReg.HKEY_LOCAL_MACHINE, subkey, name);
	}

	@Override
	public Map<String, Object> getLocalMachineValues(String subkey) {
		return getValues(WinReg.HKEY_LOCAL_MACHINE, subkey);
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
	public Map<String, Object> getCurrentUserValues(String subkey) {
		return getValues(WinReg.HKEY_CURRENT_USER, subkey);
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
			if (DEBUG) {
				CNativePlugin.log(String.format("Unable to get value for %s in %s", name, subkey), e); //$NON-NLS-1$
			}
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
			if (DEBUG) {
				CNativePlugin.log(String.format("Unable to get keyname for %s at index %d", subkey, index), e); //$NON-NLS-1$
			}
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
						null, (Pointer) null, null);

				if (rc != W32Errors.ERROR_SUCCESS) {
					throw new Win32Exception(rc);
				}

				return Native.toString(lpValueName);
			} finally {
				Advapi32Util.registryCloseKey(phkKey.getValue());
			}
		} catch (Win32Exception e) {
			if (DEBUG) {
				CNativePlugin.log(String.format("Unable to get valuename for %s at index %d", subkey, index), e); //$NON-NLS-1$
			}
			return null;
		}
	}

	private Map<String, Object> getValues(HKEY key, String subkey) {
		try {
			return Advapi32Util.registryGetValues(key, subkey);
		} catch (Win32Exception e) {
			if (DEBUG) {
				CNativePlugin.log(String.format("Unable to get values for %s", subkey), e); //$NON-NLS-1$
			}
			return Collections.emptyMap();
		}
	}
}
