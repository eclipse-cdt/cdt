/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;

/**
 * This class is an extension of JNA and everything here needs to be contributed back
 * to JNA. This class was written against JNA 5.6
 *
 * @noreference This interface is not intended to be referenced by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ConPTYKernel32 extends Kernel32 {
	ConPTYKernel32 INSTANCE = Native.load("kernel32", ConPTYKernel32.class, //$NON-NLS-1$
			com.sun.jna.win32.W32APIOptions.DEFAULT_OPTIONS);

	int PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE = 0x00020016;

	class SIZE_TByReference extends ULONG_PTRByReference {
		@Override
		public SIZE_T getValue() {
			return new SIZE_T(super.getValue().longValue());
		}
	}

	boolean CancelIoEx(HANDLE hFile, Pointer lpOverlapped);

	public static class COORD_ByValue extends COORD implements Structure.ByValue {
	}

	@Structure.FieldOrder({ "StartupInfo", "lpAttributeList" })
	class STARTUPINFOEX extends Structure {
		public STARTUPINFO StartupInfo;
		public Pointer lpAttributeList;
	}

	HRESULT CreatePseudoConsole(COORD.ByValue size, HANDLE hInput, HANDLE hOutput, DWORD dwFlags,
			HANDLEByReference phPC);

	HRESULT ResizePseudoConsole(HANDLE hPC, COORD.ByValue size);

	void ClosePseudoConsole(HANDLE hPC);

	boolean InitializeProcThreadAttributeList(Pointer lpAttributeList, DWORD dwAttributeCount, DWORD dwFlags,
			SIZE_TByReference lpSize);

	boolean UpdateProcThreadAttribute(Pointer lpAttributeList, DWORD dwFlags, DWORD_PTR Attribute, PVOID lpValue,
			SIZE_T cbSize, PVOID lpPreviousValue, SIZE_TByReference lpReturnSize);

	void DeleteProcThreadAttributeList(Pointer lpAttributeList);

	boolean CreateProcess(String lpApplicationName, String lpCommandLine, SECURITY_ATTRIBUTES lpProcessAttributes,
			SECURITY_ATTRIBUTES lpThreadAttributes, boolean bInheritHandles, DWORD dwCreationFlags,
			Pointer lpEnvironment, String lpCurrentDirectory, STARTUPINFOEX lpStartupInfo,
			PROCESS_INFORMATION lpProcessInformation);
}