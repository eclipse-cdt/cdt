/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.msw.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class WinDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IPath[] paths;
	private final Map<String, String> symbols = new HashMap<>();

	public WinDiscoveredPathInfo() {
		// Include paths
		paths = WinEnvironmentVariableSupplier.getIncludePath();

		symbols.put("_M_IX86", "600"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("_WIN32", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("_MSC_VER", "1400"); //$NON-NLS-1$ //$NON-NLS-2$

		// Microsoft specific modifiers that can be ignored
		symbols.put("__cdecl", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__fastcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__restrict", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__sptr", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__stdcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__unaligned", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__uptr", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__w64", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__clrcall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__thiscall", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__vectorcall", ""); //$NON-NLS-1$ //$NON-NLS-2$

		// Redefine some things so that the CDT parser can handle them, until there is a VC specific parser
		symbols.put("__forceinline", "__inline"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int8", "char"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int16", "short"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int32", "int"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__int64", "long long"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__pragma(X)", ""); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__nullptr", "nullptr"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__debugbreak()", "0/0"); //$NON-NLS-1$ //$NON-NLS-2$
		symbols.put("__LPREFIX(A)", "L\"##A\""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IPath[] getIncludePaths() {
		return paths;
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	@Override
	public Map<String, String> getSymbols() {
		return symbols;
	}

}
