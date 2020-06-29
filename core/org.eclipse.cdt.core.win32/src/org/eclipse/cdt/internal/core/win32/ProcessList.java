/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
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
 *     Martin Oberhuber (Wind River) - [303083] Split out the Spawner
 *******************************************************************************/
package org.eclipse.cdt.internal.core.win32;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public class ProcessList implements IProcessList {
	// TODO: Remove this inner class when JNA 5.6 is available
	private static abstract class PsapiUtil {
		public interface Psapi extends com.sun.jna.platform.win32.Psapi {
			Psapi INSTANCE = Native.loadLibrary("psapi", Psapi.class, W32APIOptions.DEFAULT_OPTIONS); //$NON-NLS-1$

			boolean EnumProcesses(int[] lpidProcess, int cb, IntByReference lpcbNeeded);
		}

		public static int[] enumProcesses() {
			int size = 0;
			int[] lpidProcess = null;
			IntByReference lpcbNeeded = new IntByReference();
			do {
				size += 1024;
				lpidProcess = new int[size];
				if (!Psapi.INSTANCE.EnumProcesses(lpidProcess, size * DWORD.SIZE, lpcbNeeded)) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
				}
			} while (size == lpcbNeeded.getValue() / DWORD.SIZE);

			return Arrays.copyOf(lpidProcess, lpcbNeeded.getValue() / DWORD.SIZE);
		}
	}

	// TODO: Remove this inner class when JNA 5.6 is available
	private static abstract class Kernel32Util extends com.sun.jna.platform.win32.Kernel32Util {
		public static final String QueryFullProcessImageName(int pid, int dwFlags) {
			HANDLE hProcess = null;
			Win32Exception we = null;

			try {
				hProcess = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_QUERY_INFORMATION | WinNT.PROCESS_VM_READ, false,
						pid);
				if (hProcess == null) {
					throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
				}
				return QueryFullProcessImageName(hProcess, dwFlags);
			} catch (Win32Exception e) {
				we = e;
				throw we; // re-throw to invoke finally block
			} finally {
				try {
					closeHandle(hProcess);
				} catch (Win32Exception e) {
					if (we == null) {
						we = e;
					} else {
						we.addSuppressed(e);
					}
				}
				if (we != null) {
					throw we;
				}
			}
		}
	}

	private IProcessInfo[] NOPROCESS = new IProcessInfo[0];

	@Override
	public IProcessInfo[] getProcessList() {
		try {
			List<IProcessInfo> processList = new ArrayList<>();

			for (int pid : PsapiUtil.enumProcesses()) {
				try {
					String name = Kernel32Util.QueryFullProcessImageName(pid, 0);
					processList.add(new ProcessInfo(pid, name));
				} catch (Win32Exception e) {
					// Intentionally ignored exception. Probable cause is access denied.
				}
			}

			return processList.toArray(new IProcessInfo[processList.size()]);
		} catch (Win32Exception e) {
			return NOPROCESS;
		}
	}
}
